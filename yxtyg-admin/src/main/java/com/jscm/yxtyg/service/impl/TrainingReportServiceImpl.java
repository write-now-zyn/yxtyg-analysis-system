package com.jscm.yxtyg.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jscm.yxtyg.common.PageResult;
import com.jscm.yxtyg.dto.TrainingRecordDTO;
import com.jscm.yxtyg.dto.TrainingReportQueryDTO;
import com.jscm.yxtyg.entity.TrainingRecord;
import com.jscm.yxtyg.entity.TrainingReport;
import com.jscm.yxtyg.exception.BusinessException;
import com.jscm.yxtyg.mapper.TrainingReportMapper;
import com.jscm.yxtyg.service.TrainingRecordService;
import com.jscm.yxtyg.service.TrainingReportService;
import com.jscm.yxtyg.vo.TrainingReportDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 转培上报服务实现类
 */
@Slf4j
@Service
public class TrainingReportServiceImpl extends ServiceImpl<TrainingReportMapper, TrainingReport> implements TrainingReportService {

    @Autowired
    private TrainingRecordService trainingRecordService;

    @Override
    public PageResult<TrainingReport> queryPage(TrainingReportQueryDTO queryDTO) {
        log.debug("查询转培上报列表，条件：{}", queryDTO);
        
        LambdaQueryWrapper<TrainingReport> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(queryDTO.getCity())) {
            wrapper.eq(TrainingReport::getCity, queryDTO.getCity());
        }
        if (StringUtils.hasText(queryDTO.getReportMonth())) {
            wrapper.eq(TrainingReport::getReportMonth, queryDTO.getReportMonth());
        }
        
        // 培训内容或培训主体关键词搜索需要关联查询培训记录表
        if (StringUtils.hasText(queryDTO.getTrainingContent()) || StringUtils.hasText(queryDTO.getTrainingSubject())) {
            log.debug("需要关联查询培训记录表");
            
            LambdaQueryWrapper<TrainingRecord> recordWrapper = new LambdaQueryWrapper<>();
            if (StringUtils.hasText(queryDTO.getTrainingContent())) {
                recordWrapper.like(TrainingRecord::getTrainingContent, queryDTO.getTrainingContent());
            }
            if (StringUtils.hasText(queryDTO.getTrainingSubject())) {
                recordWrapper.like(TrainingRecord::getTrainingSubject, queryDTO.getTrainingSubject());
            }
            List<TrainingRecord> records = trainingRecordService.list(recordWrapper);
            if (!records.isEmpty()) {
                List<Long> reportIds = new ArrayList<>();
                for (TrainingRecord record : records) {
                    if (!reportIds.contains(record.getReportId())) {
                        reportIds.add(record.getReportId());
                    }
                }
                wrapper.in(TrainingReport::getId, reportIds);
                log.debug("关联查询到{}个上报记录ID", reportIds.size());
            } else {
                log.debug("没有匹配的培训记录，返回空结果");
                return PageResult.of(0L, queryDTO.getCurrent(), queryDTO.getSize(), new ArrayList<>());
            }
        }
        
        wrapper.orderByDesc(TrainingReport::getCreateTime);
        
        Page<TrainingReport> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<TrainingReport> result = this.page(page, wrapper);
        
        log.info("查询转培上报列表完成，共{}条记录", result.getTotal());
        return PageResult.of(result);
    }

    @Override
    public TrainingReportDetailVO getDetail(Long id) {
        log.debug("获取转培上报详情，ID：{}", id);
        
        TrainingReport report = this.getById(id);
        if (report == null) {
            log.warn("转培上报记录不存在，ID：{}", id);
            return null;
        }
        
        TrainingReportDetailVO vo = new TrainingReportDetailVO();
        vo.setId(report.getId());
        vo.setCity(report.getCity());
        vo.setReportMonth(report.getReportMonth());
        vo.setReportTime(report.getReportTime());
        vo.setReporter(report.getReporter());
        vo.setRecordCount(report.getRecordCount());
        vo.setTrainingPersonCount(report.getTrainingPersonCount());
        vo.setCreateTime(report.getCreateTime());
        
        // 查询培训记录
        List<TrainingRecord> records = trainingRecordService.listByReportId(id);
        List<TrainingRecordDTO> recordDTOs = new ArrayList<>();
        for (TrainingRecord record : records) {
            TrainingRecordDTO dto = new TrainingRecordDTO();
            dto.setId(record.getId());
            dto.setTrainingTime(record.getTrainingTime() != null ? 
                    record.getTrainingTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : null);
            dto.setOrganizer(record.getOrganizer());
            dto.setTrainingSubject(record.getTrainingSubject());
            dto.setCoverageCount(record.getCoverageCount());
            dto.setTrainingContent(record.getTrainingContent());
            dto.setScreenshotName(record.getScreenshotName());
            dto.setScreenshotType(record.getScreenshotType());
            // 不返回图片Base64，改为按需加载（异步获取图片接口：/record/image/{id}）
            dto.setHasScreenshot(record.getScreenshot() != null && record.getScreenshot().length > 0);
            
            recordDTOs.add(dto);
        }
        vo.setRecords(recordDTOs);
        
        log.debug("获取转培上报详情成功，地市：{}，月份：{}，培训记录数：{}", 
                report.getCity(), report.getReportMonth(), recordDTOs.size());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWithRecords(TrainingReportDetailVO vo) {
        log.info("保存转培上报记录，ID：{}，地市：{}，月份：{}，培训记录数：{}", 
                vo.getId(), vo.getCity(), vo.getReportMonth(), vo.getRecords() != null ? vo.getRecords().size() : 0);
        
        // 计算培训人数（所有培训记录的覆盖人数之和）
        int trainingPersonCount = 0;
        if (vo.getRecords() != null) {
            for (TrainingRecordDTO record : vo.getRecords()) {
                if (record.getCoverageCount() != null) {
                    trainingPersonCount += record.getCoverageCount();
                }
            }
        }
        
        // 检查是否已存在（地市+培训月份唯一）
        LambdaQueryWrapper<TrainingReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrainingReport::getCity, vo.getCity());
        wrapper.eq(TrainingReport::getReportMonth, vo.getReportMonth());
        // 编辑时排除自身
        if (vo.getId() != null) {
            wrapper.ne(TrainingReport::getId, vo.getId());
        }
        TrainingReport existReport = this.getOne(wrapper);
        
        // 新增时检查是否已存在
        if (vo.getId() == null && existReport != null) {
            log.warn("新增失败，地市 {} 培训月份 {} 已存在记录，ID：{}", vo.getCity(), vo.getReportMonth(), existReport.getId());
            throw new BusinessException(400, "该地市当前培训月份已有上报记录，请前往编辑");
        }
        
        TrainingReport report = new TrainingReport();
        report.setCity(vo.getCity());
        report.setReportMonth(vo.getReportMonth());
        report.setReportTime(vo.getReportTime());
        report.setReporter(vo.getReporter());
        report.setRecordCount(vo.getRecords() != null ? vo.getRecords().size() : 0);
        report.setTrainingPersonCount(trainingPersonCount);
        
        // 用于缓存原有培训记录（编辑模式下保留图片）
        Map<Long, TrainingRecord> existingRecordsMap = new HashMap<>();
        
        if (vo.getId() != null) {
            // 编辑模式
            log.info("更新记录，ID：{}", vo.getId());
            report.setId(vo.getId());
            report.setCreateTime(this.getById(vo.getId()).getCreateTime());
            report.setUpdateTime(LocalDateTime.now());
            this.updateById(report);
            
            // 删除前先缓存原有培训记录（用于保留图片）
            List<TrainingRecord> existingRecords = trainingRecordService.listByReportId(report.getId());
            for (TrainingRecord existing : existingRecords) {
                existingRecordsMap.put(existing.getId(), existing);
            }
            
            // 删除旧的培训记录
            trainingRecordService.deleteByReportId(report.getId());
        } else {
            log.info("新增转培上报记录");
            report.setCreateTime(LocalDateTime.now());
            report.setUpdateTime(LocalDateTime.now());
            this.save(report);
        }
        
        // 保存培训记录
        if (vo.getRecords() != null && !vo.getRecords().isEmpty()) {
            for (TrainingRecordDTO recordDTO : vo.getRecords()) {
                TrainingRecord record = new TrainingRecord();
                record.setReportId(report.getId());
                if (StringUtils.hasText(recordDTO.getTrainingTime())) {
                    // 支持 yyyy-MM-dd 格式
                    record.setTrainingTime(LocalDate.parse(recordDTO.getTrainingTime()).atStartOfDay());
                }
                record.setOrganizer(recordDTO.getOrganizer());
                record.setTrainingSubject(recordDTO.getTrainingSubject());
                record.setCoverageCount(recordDTO.getCoverageCount());
                record.setTrainingContent(recordDTO.getTrainingContent());
                record.setScreenshotName(recordDTO.getScreenshotName());
                record.setScreenshotType(recordDTO.getScreenshotType());
                
                // Base64转二进制
                if (StringUtils.hasText(recordDTO.getScreenshotBase64())) {
                    // 有新上传的图片
                    record.setScreenshot(Base64.getDecoder().decode(recordDTO.getScreenshotBase64()));
                } else if (recordDTO.getId() != null && Boolean.TRUE.equals(recordDTO.getHasScreenshot())) {
                    // 编辑模式：有记录ID且原来有图片，但没有新上传图片，从缓存中获取原有图片保留
                    TrainingRecord existingRecord = existingRecordsMap.get(recordDTO.getId());
                    if (existingRecord != null && existingRecord.getScreenshot() != null) {
                        record.setScreenshot(existingRecord.getScreenshot());
                        if (!StringUtils.hasText(recordDTO.getScreenshotName())) {
                            record.setScreenshotName(existingRecord.getScreenshotName());
                        }
                        if (!StringUtils.hasText(recordDTO.getScreenshotType())) {
                            record.setScreenshotType(existingRecord.getScreenshotType());
                        }
                    }
                }
                
                record.setCreateTime(LocalDateTime.now());
                trainingRecordService.save(record);
            }
            log.info("保存{}条培训记录完成", vo.getRecords().size());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWithRecords(Long id) {
        log.info("删除转培上报记录，ID：{}", id);
        
        // 删除培训记录
        trainingRecordService.deleteByReportId(id);
        // 删除上报记录
        this.removeById(id);
        
        log.info("删除转培上报记录完成，ID：{}", id);
    }

    @Override
    public void exportExcel(TrainingReportQueryDTO queryDTO, HttpServletResponse response) {
        log.info("开始导出转培上报Excel，条件：{}", queryDTO);
        
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("转培上报记录", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            PageResult<TrainingReport> pageResult = queryPage(queryDTO);
            
            EasyExcel.write(response.getOutputStream(), TrainingReport.class)
                    .sheet("转培上报记录")
                    .doWrite(pageResult.getRecords());
            
            log.info("导出转培上报Excel完成，共{}条记录", pageResult.getTotal());
        } catch (IOException e) {
            log.error("导出转培上报Excel失败", e);
            throw new RuntimeException("导出失败", e);
        }
    }

}
