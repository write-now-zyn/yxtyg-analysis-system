package com.jscm.yxtyg.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jscm.yxtyg.config.RecommendProperties;
import com.jscm.yxtyg.entity.ModelConfig;
import com.jscm.yxtyg.exception.BusinessException;
import com.jscm.yxtyg.service.EmbeddingService;
import com.jscm.yxtyg.service.LlmClientService;
import com.jscm.yxtyg.service.ModelConfigService;
import com.jscm.yxtyg.service.RecommendService;
import com.jscm.yxtyg.service.VectorStoreService;
import com.jscm.yxtyg.vo.RecommendPersonVO;
import com.jscm.yxtyg.vo.RecommendResultVO;
import com.jscm.yxtyg.vo.SimilarTicketVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 智能推荐服务
 */
@Slf4j
@Service
public class RecommendServiceImpl implements RecommendService {

    @Autowired
    private ModelConfigService modelConfigService;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private VectorStoreService vectorStoreService;

    @Autowired
    private RecommendProperties recommendProperties;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LlmClientService llmClientService;

    @Override
    public RecommendResultVO recommend(String content) {
        if (!StringUtils.hasText(content)) {
            throw new BusinessException(400, "工单内容不能为空");
        }

        List<Double> embedding = embeddingService.embed(content);
        List<SimilarTicketVO> similarTickets = vectorStoreService.searchByEmbedding(embedding, recommendProperties.getTopK());

        RecommendResultVO fallback = buildFallbackResult(content, similarTickets);
        if (CollectionUtils.isEmpty(similarTickets)) {
            fallback.setReason("未检索到可参考的历史工单");
            return fallback;
        }

        try {
            ModelConfig modelConfig = modelConfigService.getDefaultConfig();
            if (modelConfig == null) {
                log.warn("未找到默认模型配置，使用规则兜底返回");
                return fallback;
            }

            String rawContent = callModel(modelConfig, content, similarTickets);
            RecommendResultVO modelResult = parseRecommendResult(rawContent);
            modelResult.setSimilarTickets(similarTickets);

            if (CollectionUtils.isEmpty(modelResult.getRecommendPersons())) {
                return fallback;
            }

            if (!StringUtils.hasText(modelResult.getReason())) {
                modelResult.setReason(fallback.getReason());
            }
            return modelResult;
        } catch (Exception e) {
            log.error("智能推荐失败，改用规则兜底", e);
            return fallback;
        }
    }

    private String callModel(ModelConfig modelConfig, String content, List<SimilarTicketVO> similarTickets) throws Exception {
        String modelName = resolveModelName(modelConfig);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(buildMessage("system", buildSystemPrompt()));
        messages.add(buildMessage("user", buildUserPrompt(content, similarTickets)));

        String result = llmClientService.chat(modelConfig, modelName, messages,
                BigDecimal.valueOf(0.2D), BigDecimal.valueOf(0.9D), null,
                null, recommendProperties.getLlmTimeoutMs());
        if (!StringUtils.hasText(result)) {
            throw new BusinessException(500, "推荐模型未返回有效内容");
        }
        return result;
    }

    private RecommendResultVO parseRecommendResult(String rawContent) throws Exception {
        String cleaned = cleanJsonContent(rawContent);
        JsonNode root = objectMapper.readTree(cleaned);

        RecommendResultVO result = new RecommendResultVO();
        result.setReason(root.path("reason").asText(""));

        JsonNode recommendPersonsNode = root.path("recommendPersons");
        if (recommendPersonsNode.isArray()) {
            List<RecommendPersonVO> persons = new ArrayList<>();
            for (JsonNode node : recommendPersonsNode) {
                RecommendPersonVO person = new RecommendPersonVO();
                person.setName(node.path("name").asText(""));
                if (node.has("confidence")) {
                    person.setConfidence(node.path("confidence").asDouble());
                }
                person.setReason(node.path("reason").asText(""));
                if (node.has("ticketCount")) {
                    person.setTicketCount(node.path("ticketCount").asInt());
                }
                if (StringUtils.hasText(person.getName())) {
                    persons.add(person);
                }
            }
            result.setRecommendPersons(persons);
        }
        return result;
    }

    private RecommendResultVO buildFallbackResult(String content, List<SimilarTicketVO> similarTickets) {
        RecommendResultVO result = new RecommendResultVO();
        result.setSimilarTickets(similarTickets);

        Map<String, List<SimilarTicketVO>> grouped = similarTickets.stream()
                .filter(item -> StringUtils.hasText(item.getHandlerName()))
                .collect(Collectors.groupingBy(SimilarTicketVO::getHandlerName, LinkedHashMap::new, Collectors.toList()));

        List<RecommendPersonVO> persons = grouped.entrySet().stream()
                .map(entry -> {
                    double maxScore = entry.getValue().stream()
                            .map(SimilarTicketVO::getSimilarityScore)
                            .filter(score -> score != null)
                            .max(Comparator.naturalOrder())
                            .orElse(0D);
                    RecommendPersonVO person = new RecommendPersonVO();
                    person.setName(entry.getKey());
                    person.setTicketCount(entry.getValue().size());
                    person.setConfidence(maxScore);
                    person.setReason("历史相似工单中该处理人出现频次较高");
                    return person;
                })
                .sorted(Comparator.comparing(RecommendPersonVO::getTicketCount, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(RecommendPersonVO::getConfidence, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        result.setRecommendPersons(persons);
        result.setReason(persons.isEmpty()
                ? "未从相似工单中提取到有效处理人"
                : "基于相似工单处理人出现频次和相似度进行兜底推荐");
        return result;
    }

    private String buildSystemPrompt() {
        return "你是工单处理人推荐助手。"
                + "请只根据提供的当前工单内容和历史相似工单信息，输出推荐处理人结果。"
                + "不要编造不存在的人员。"
                + "只返回 JSON，不要附加解释。"
                + "JSON 格式必须是："
                + "{\"reason\":\"总体推荐理由\",\"recommendPersons\":[{\"name\":\"处理人\",\"confidence\":0.85,\"reason\":\"推荐理由\",\"ticketCount\":3}]}";
    }

    private String buildUserPrompt(String content, List<SimilarTicketVO> similarTickets) {
        StringBuilder builder = new StringBuilder();
        builder.append("当前工单内容：\n")
                .append(abbreviate(content, recommendProperties.getMaxCurrentContentLength()))
                .append("\n\n");
        builder.append("历史相似工单列表：\n");

        int limit = Math.min(similarTickets.size(), recommendProperties.getMaxPromptTickets());
        for (int i = 0; i < limit; i++) {
            SimilarTicketVO ticket = similarTickets.get(i);
            builder.append(i + 1).append(". ")
                    .append("工单号=").append(defaultString(ticket.getOrderNo()))
                    .append("；处理人=").append(defaultString(ticket.getHandlerName()))
                    .append("；地市=").append(defaultString(ticket.getCity()))
                    .append("；状态=").append(defaultString(ticket.getStatus()))
                    .append("；相似度=").append(ticket.getSimilarityScore() == null ? 0D : ticket.getSimilarityScore())
                    .append("\n");
            builder.append("工单内容摘要：")
                    .append(abbreviate(ticket.getContentSummary(), recommendProperties.getMaxHistoryContentLength()))
                    .append("\n");
            builder.append("解决方案摘要：")
                    .append(abbreviate(ticket.getSolutionSummary(), recommendProperties.getMaxHistorySolutionLength()))
                    .append("\n\n");
        }

        builder.append("请输出 1-3 个推荐处理人，并给出简洁理由。");
        return builder.toString();
    }

    private String cleanJsonContent(String content) {
        String cleaned = content.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("^```json", "").replaceFirst("^```", "").replaceFirst("```$", "").trim();
        }
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start >= 0 && end > start) {
            cleaned = cleaned.substring(start, end + 1);
        }
        return cleaned;
    }

    private String resolveModelName(ModelConfig modelConfig) {
        if (StringUtils.hasText(modelConfig.getModels())) {
            try {
                List<String> models = objectMapper.readValue(modelConfig.getModels(), new TypeReference<List<String>>() {});
                if (!CollectionUtils.isEmpty(models) && StringUtils.hasText(models.get(0))) {
                    return models.get(0);
                }
            } catch (Exception e) {
                log.warn("解析模型列表失败，将继续使用默认兜底模型", e);
            }
        }
        return "qwen3:8b";
    }

    private Map<String, String> buildMessage(String role, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
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
}
