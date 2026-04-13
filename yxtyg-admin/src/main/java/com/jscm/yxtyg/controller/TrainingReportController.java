package com.jscm.yxtyg.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jscm.yxtyg.common.PageResult;
import com.jscm.yxtyg.common.Result;
import com.jscm.yxtyg.dto.TrainingReportQueryDTO;
import com.jscm.yxtyg.entity.TrainingRecord;
import com.jscm.yxtyg.entity.TrainingReport;
import com.jscm.yxtyg.mapper.TrainingRecordMapper;
import com.jscm.yxtyg.service.TrainingReportService;
import com.jscm.yxtyg.vo.TrainingReportDetailVO;
import com.jscm.yxtyg.vo.TrainingReportVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 转培上报统计控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/training")
public class TrainingReportController {

    @Resource
    private TrainingReportService trainingReportService;

    @Resource
    private TrainingRecordMapper trainingRecordMapper;

    /**
     * 分页查询上报列表
     */
    @GetMapping("/report/list")
    public Result<Page<TrainingReportVO>> list(TrainingReportQueryDTO queryDTO) {
        log.info("收到查询转培上报列表请求，条件：{}", queryDTO);
        PageResult<TrainingReport> pageResult = trainingReportService.queryPage(queryDTO);
        
        // 转换为VO
        Page<TrainingReportVO> voPage = new Page<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
        List<TrainingReportVO> voList = new ArrayList<>();
        for (TrainingReport report : pageResult.getRecords()) {
            TrainingReportVO vo = new TrainingReportVO();
            BeanUtils.copyProperties(report, vo);
            voList.add(vo);
        }
        voPage.setRecords(voList);
        
        return Result.success(voPage);
    }

    /**
     * 获取上报详情
     */
    @GetMapping("/report/detail/{id}")
    public Result<TrainingReportDetailVO> detail(@PathVariable Long id) {
        log.info("收到获取转培上报详情请求，ID：{}", id);
        return Result.success(trainingReportService.getDetail(id));
    }

    /**
     * 新增培训上报
     */
    @PostMapping("/report/add")
    public Result<Void> add(@RequestBody @Validated TrainingReportDetailVO vo) {
        log.info("收到新增培训上报请求，地市：{}，月份：{}，培训记录数：{}", 
                vo.getCity(), vo.getReportMonth(), vo.getRecords() != null ? vo.getRecords().size() : 0);
        trainingReportService.saveWithRecords(vo);
        log.info("新增培训上报成功");
        return Result.success();
    }

    /**
     * 更新培训上报
     */
    @PutMapping("/report/update")
    public Result<Void> update(@RequestBody @Validated TrainingReportDetailVO vo) {
        log.info("收到更新培训上报请求，ID：{}，地市：{}，月份：{}", 
                vo.getId(), vo.getCity(), vo.getReportMonth());
        trainingReportService.saveWithRecords(vo);
        log.info("更新培训上报成功，ID：{}", vo.getId());
        return Result.success();
    }

    /**
     * 删除培训上报
     */
    @DeleteMapping("/report/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("收到删除培训上报请求，ID：{}", id);
        trainingReportService.deleteWithRecords(id);
        log.info("删除培训上报成功，ID：{}", id);
        return Result.success();
    }

    /**
     * 导出Excel
     */
    @GetMapping("/export")
    public void export(TrainingReportQueryDTO queryDTO, HttpServletResponse response) {
        log.info("收到导出转培上报Excel请求，条件：{}", queryDTO);
        trainingReportService.exportExcel(queryDTO, response);
    }

    /**
     * 获取培训截图图片
     */
    @GetMapping("/record/image/{id}")
    public void getImage(@PathVariable Long id, HttpServletResponse response) {
        log.info("收到获取培训截图请求，记录 ID：{}", id);
        try {
            TrainingRecord record = trainingRecordMapper.selectById(id);
            if (record == null || record.getScreenshot() == null) {
                log.warn("培训截图不存在，记录 ID：{}", id);
                response.setStatus(404);
                return;
            }
            
            String contentType = record.getScreenshotType();
            if (contentType == null) {
                contentType = MediaType.IMAGE_JPEG_VALUE;
            }
            
            response.setContentType(contentType);
            // 对中文文件名进行 URL 编码，避免乱码
            String encodedFilename = java.net.URLEncoder.encode(record.getScreenshotName(), "UTF-8").replace("+", "%20");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline;filename*=UTF-8''" + encodedFilename);
            response.getOutputStream().write(record.getScreenshot());
            response.getOutputStream().flush();
            log.info("返回培训截图成功，记录 ID：{}，大小：{}字节", id, record.getScreenshot().length);
        } catch (Exception e) {
            log.error("获取培训截图失败，记录 ID：{}", id, e);
            response.setStatus(500);
        }
    }
}
