package com.jscm.yxtyg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jscm.yxtyg.config.RecommendProperties;
import com.jscm.yxtyg.config.VectorProperties;
import com.jscm.yxtyg.entity.WorkOrder;
import com.jscm.yxtyg.exception.BusinessException;
import com.jscm.yxtyg.mapper.WorkOrderMapper;
import com.jscm.yxtyg.service.EmbeddingService;
import com.jscm.yxtyg.service.VectorStoreService;
import com.jscm.yxtyg.vector.VectorStoreDocument;
import com.jscm.yxtyg.vector.WorkOrderVectorRecord;
import com.jscm.yxtyg.vo.SimilarTicketVO;
import com.jscm.yxtyg.vo.VectorStatsVO;
import com.jscm.yxtyg.vo.VectorSyncResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * 向量文件存储服务
 */
@Slf4j
@Service
public class VectorStoreServiceImpl implements VectorStoreService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String DEFAULT_FILE_NAME = "vector-store.json";

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    @Autowired
    private WorkOrderMapper workOrderMapper;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VectorProperties vectorProperties;

    @Autowired
    private RecommendProperties recommendProperties;

    @Override
    public List<SimilarTicketVO> match(String content, Integer limit) {
        if (!StringUtils.hasText(content)) {
            throw new BusinessException(400, "工单内容不能为空");
        }
        List<Double> embedding = embeddingService.embed(content);
        return searchByEmbedding(embedding, limit);
    }

    @Override
    public List<SimilarTicketVO> searchByEmbedding(List<Double> embedding, Integer limit) {
        if (embedding == null || embedding.isEmpty()) {
            return Collections.emptyList();
        }

        VectorStoreDocument document = readDocument();
        if (document.getVectors() == null || document.getVectors().isEmpty()) {
            return Collections.emptyList();
        }

        int actualLimit = limit == null || limit <= 0 ? recommendProperties.getTopK() : limit;
        return document.getVectors().stream()
                .map(record -> toSimilarTicket(record, cosineSimilarity(embedding, record.getVector())))
                .sorted(Comparator.comparing(SimilarTicketVO::getSimilarityScore, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(actualLimit)
                .collect(Collectors.toList());
    }

    @Override
    public VectorStatsVO stats() {
        VectorStoreDocument document = readDocument();

        VectorStatsVO stats = new VectorStatsVO();
        stats.setVectorizedCount((long) document.getVectors().size());
        stats.setVectorizableCount(countVectorizableWorkOrders());
        stats.setTotalWorkOrderCount((long) workOrderMapper.selectCount(null));
        stats.setLastSyncTime(document.getLastSyncTime());
        return stats;
    }

    @Override
    public VectorSyncResultVO syncIncremental() {
        List<WorkOrder> vectorizableWorkOrders = listVectorizableWorkOrders();
        Map<String, WorkOrder> workOrderMap = vectorizableWorkOrders.stream()
                .collect(Collectors.toMap(WorkOrder::getOrderNo, item -> item, (left, right) -> right));

        VectorStoreDocument document = readDocument();
        Map<String, WorkOrderVectorRecord> existingMap = document.getVectors().stream()
                .collect(Collectors.toMap(WorkOrderVectorRecord::getOrderNo, item -> item, (left, right) -> right, HashMap::new));

        int addedCount = 0;
        int updatedCount = 0;
        int skippedCount = 0;

        for (WorkOrder workOrder : vectorizableWorkOrders) {
            WorkOrderVectorRecord existing = existingMap.get(workOrder.getOrderNo());
            if (existing == null) {
                existingMap.put(workOrder.getOrderNo(), buildRecord(workOrder, null));
                addedCount++;
                continue;
            }

            String contentHash = buildContentHash(workOrder.getContent());
            boolean contentChanged = !Objects.equals(contentHash, existing.getContentHash());
            boolean metaChanged = isMetadataChanged(existing, workOrder);

            if (contentChanged) {
                existingMap.put(workOrder.getOrderNo(), buildRecord(workOrder, existing.getCreatedAt()));
                updatedCount++;
            } else if (metaChanged) {
                refreshRecordMetadata(existing, workOrder);
                updatedCount++;
            } else {
                skippedCount++;
            }
        }

        Set<String> validOrderNos = new HashSet<>(workOrderMap.keySet());
        int removedCount = 0;
        List<WorkOrderVectorRecord> retained = new ArrayList<>();
        for (WorkOrderVectorRecord record : existingMap.values()) {
            if (validOrderNos.contains(record.getOrderNo())) {
                retained.add(record);
            } else {
                removedCount++;
            }
        }

        retained.sort(Comparator.comparing(WorkOrderVectorRecord::getOrderNo));
        document.setVectors(retained);
        document.setLastSyncTime(nowString());
        writeDocument(document);

        VectorSyncResultVO result = new VectorSyncResultVO();
        result.setAddedCount(addedCount);
        result.setUpdatedCount(updatedCount);
        result.setRemovedCount(removedCount);
        result.setSkippedCount(skippedCount);
        result.setTotalCount(vectorizableWorkOrders.size());
        result.setLastSyncTime(document.getLastSyncTime());
        return result;
    }

    @Override
    public void syncWorkOrderById(Long workOrderId) {
        if (workOrderId == null) {
            return;
        }

        WorkOrder workOrder = workOrderMapper.selectById(workOrderId);
        if (workOrder == null) {
            return;
        }

        if (!isVectorizable(workOrder)) {
            removeByOrderNo(workOrder.getOrderNo());
            return;
        }

        readWriteLock.writeLock().lock();
        try {
            VectorStoreDocument document = loadDocumentWithoutLock();
            Map<String, WorkOrderVectorRecord> recordMap = document.getVectors().stream()
                    .collect(Collectors.toMap(WorkOrderVectorRecord::getOrderNo, item -> item, (left, right) -> right, HashMap::new));

            WorkOrderVectorRecord existing = recordMap.get(workOrder.getOrderNo());
            String contentHash = buildContentHash(workOrder.getContent());

            if (existing == null || !Objects.equals(contentHash, existing.getContentHash())) {
                recordMap.put(workOrder.getOrderNo(), buildRecord(workOrder, existing == null ? null : existing.getCreatedAt()));
            } else {
                refreshRecordMetadata(existing, workOrder);
            }

            document.setVectors(recordMap.values().stream()
                    .sorted(Comparator.comparing(WorkOrderVectorRecord::getOrderNo))
                    .collect(Collectors.toList()));
            document.setLastSyncTime(nowString());
            writeDocumentWithoutLock(document);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public void removeByOrderNo(String orderNo) {
        if (!StringUtils.hasText(orderNo)) {
            return;
        }

        readWriteLock.writeLock().lock();
        try {
            VectorStoreDocument document = loadDocumentWithoutLock();
            boolean removed = document.getVectors().removeIf(item -> orderNo.equals(item.getOrderNo()));
            if (removed) {
                document.setLastSyncTime(nowString());
                writeDocumentWithoutLock(document);
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    private SimilarTicketVO toSimilarTicket(WorkOrderVectorRecord record, double similarity) {
        SimilarTicketVO vo = new SimilarTicketVO();
        vo.setOrderNo(record.getOrderNo());
        vo.setHandlerName(record.getHandlerName());
        vo.setCity(record.getCity());
        vo.setStatus(record.getStatus());
        vo.setHandleUnit(record.getHandleUnit());
        vo.setContentSummary(abbreviate(record.getContentText(), recommendProperties.getMaxTicketContentLength()));
        vo.setSolutionSummary(abbreviate(record.getSolutionSummary(), recommendProperties.getMaxHistorySolutionLength()));
        vo.setSimilarityScore(similarity);
        return vo;
    }

    private WorkOrderVectorRecord buildRecord(WorkOrder workOrder, String createdAt) {
        List<Double> vector = embeddingService.embed(workOrder.getContent());
        if (vector == null || vector.isEmpty()) {
            throw new BusinessException(500, "工单向量化失败：" + workOrder.getOrderNo());
        }

        WorkOrderVectorRecord record = new WorkOrderVectorRecord();
        record.setOrderNo(workOrder.getOrderNo());
        record.setContentHash(buildContentHash(workOrder.getContent()));
        record.setContentText(defaultString(workOrder.getContent()));
        record.setHandlerName(defaultString(workOrder.getHandlerName()));
        record.setSolutionSummary(defaultString(workOrder.getSolutionHistoryStr()));
        record.setCity(defaultString(workOrder.getCity()));
        record.setStatus(defaultString(workOrder.getStatus()));
        record.setHandleUnit(defaultString(workOrder.getHandleUnit()));
        record.setVector(vector);
        record.setCreatedAt(StringUtils.hasText(createdAt) ? createdAt : nowString());
        record.setUpdatedAt(nowString());
        return record;
    }

    private void refreshRecordMetadata(WorkOrderVectorRecord record, WorkOrder workOrder) {
        record.setContentText(defaultString(workOrder.getContent()));
        record.setHandlerName(defaultString(workOrder.getHandlerName()));
        record.setSolutionSummary(defaultString(workOrder.getSolutionHistoryStr()));
        record.setCity(defaultString(workOrder.getCity()));
        record.setStatus(defaultString(workOrder.getStatus()));
        record.setHandleUnit(defaultString(workOrder.getHandleUnit()));
        record.setUpdatedAt(nowString());
    }

    private boolean isMetadataChanged(WorkOrderVectorRecord record, WorkOrder workOrder) {
        return !Objects.equals(defaultString(workOrder.getHandlerName()), defaultString(record.getHandlerName()))
                || !Objects.equals(defaultString(workOrder.getSolutionHistoryStr()), defaultString(record.getSolutionSummary()))
                || !Objects.equals(defaultString(workOrder.getCity()), defaultString(record.getCity()))
                || !Objects.equals(defaultString(workOrder.getStatus()), defaultString(record.getStatus()))
                || !Objects.equals(defaultString(workOrder.getHandleUnit()), defaultString(record.getHandleUnit()))
                || !Objects.equals(defaultString(workOrder.getContent()), defaultString(record.getContentText()));
    }

    private String buildContentHash(String content) {
        return DigestUtils.md5DigestAsHex(defaultString(content).getBytes(StandardCharsets.UTF_8));
    }

    private long countVectorizableWorkOrders() {
        return listVectorizableWorkOrders().size();
    }

    private List<WorkOrder> listVectorizableWorkOrders() {
        List<String> statuses = vectorProperties.getVectorizableStatuses();
        if (statuses == null || statuses.isEmpty()) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(WorkOrder::getStatus, statuses)
                .isNotNull(WorkOrder::getHandlerName)
                .ne(WorkOrder::getHandlerName, "")
                .orderByAsc(WorkOrder::getOrderNo);
        return workOrderMapper.selectList(wrapper);
    }

    private boolean isVectorizable(WorkOrder workOrder) {
        if (workOrder == null) {
            return false;
        }
        return vectorProperties.getVectorizableStatuses().contains(workOrder.getStatus())
                && StringUtils.hasText(workOrder.getHandlerName())
                && StringUtils.hasText(workOrder.getContent());
    }

    private double cosineSimilarity(List<Double> source, List<Double> target) {
        if (source == null || target == null || source.isEmpty() || target.isEmpty()) {
            return 0D;
        }
        int size = Math.min(source.size(), target.size());
        double dot = 0D;
        double sourceNorm = 0D;
        double targetNorm = 0D;
        for (int i = 0; i < size; i++) {
            double left = source.get(i) == null ? 0D : source.get(i);
            double right = target.get(i) == null ? 0D : target.get(i);
            dot += left * right;
            sourceNorm += left * left;
            targetNorm += right * right;
        }
        if (sourceNorm == 0D || targetNorm == 0D) {
            return 0D;
        }
        return dot / (Math.sqrt(sourceNorm) * Math.sqrt(targetNorm));
    }

    private VectorStoreDocument readDocument() {
        readWriteLock.readLock().lock();
        try {
            return loadDocumentWithoutLock();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    private VectorStoreDocument loadDocumentWithoutLock() {
        try {
            Path storeFile = resolveStoreFile();
            if (Files.notExists(storeFile)) {
                return new VectorStoreDocument();
            }
            byte[] bytes = Files.readAllBytes(storeFile);
            if (bytes.length == 0) {
                return new VectorStoreDocument();
            }
            return objectMapper.readValue(bytes, VectorStoreDocument.class);
        } catch (IOException e) {
            log.error("读取向量文件失败", e);
            throw new BusinessException(500, "读取向量文件失败：" + e.getMessage());
        }
    }

    private void writeDocument(VectorStoreDocument document) {
        readWriteLock.writeLock().lock();
        try {
            writeDocumentWithoutLock(document);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    private void writeDocumentWithoutLock(VectorStoreDocument document) {
        try {
            Path storeFile = resolveStoreFile();
            Files.createDirectories(storeFile.getParent());
            Path tempFile = storeFile.resolveSibling(storeFile.getFileName().toString() + ".tmp");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(tempFile.toFile(), document);
            Files.move(tempFile, storeFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            log.error("写入向量文件失败", e);
            throw new BusinessException(500, "写入向量文件失败：" + e.getMessage());
        }
    }

    private Path resolveStoreFile() {
        String configuredPath = vectorProperties.getStore() == null ? null : vectorProperties.getStore().getPath();
        if (!StringUtils.hasText(configuredPath)) {
            configuredPath = "./data/vectors";
        }
        Path path = Paths.get(configuredPath);
        if (configuredPath.endsWith(".json")) {
            return path.toAbsolutePath();
        }
        return path.resolve(DEFAULT_FILE_NAME).toAbsolutePath();
    }

    private String abbreviate(String text, Integer maxLength) {
        if (!StringUtils.hasText(text) || maxLength == null || maxLength <= 0 || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private String nowString() {
        return LocalDateTime.now().format(DATE_TIME_FORMATTER);
    }
}
