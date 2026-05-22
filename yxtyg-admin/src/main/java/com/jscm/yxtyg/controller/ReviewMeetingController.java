package com.jscm.yxtyg.controller;

import com.jscm.yxtyg.common.PageResult;
import com.jscm.yxtyg.common.Result;
import com.jscm.yxtyg.dto.ReviewMeetingQueryDTO;
import com.jscm.yxtyg.entity.ReviewMeeting;
import com.jscm.yxtyg.security.PermissionConstants;
import com.jscm.yxtyg.security.RequirePermissions;
import com.jscm.yxtyg.service.ReviewMeetingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

/**
 * 评审会议纪要控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/review")
@RequirePermissions(PermissionConstants.DATA_MANAGE)
public class ReviewMeetingController {

    @Autowired
    private ReviewMeetingService reviewMeetingService;

    /**
     * 分页查询会议纪要
     */
    @GetMapping("/list")
    public Result<PageResult<ReviewMeeting>> list(ReviewMeetingQueryDTO queryDTO) {
        log.info("收到查询评审会议纪要列表请求，条件：{}", queryDTO);
        return Result.success(reviewMeetingService.queryPage(queryDTO));
    }

    /**
     * 根据ID获取会议纪要
     */
    @GetMapping("/{id}")
    public Result<ReviewMeeting> getById(@PathVariable Long id) {
        log.info("收到获取会议纪要详情请求，ID：{}", id);
        return Result.success(reviewMeetingService.getById(id));
    }

    /**
     * 新增会议纪要
     */
    @PostMapping("/add")
    public Result<Void> add(@RequestBody ReviewMeeting reviewMeeting) {
        log.info("收到新增会议纪要请求：{}", reviewMeeting);
        reviewMeeting.setCreateTime(LocalDateTime.now());
        reviewMeeting.setUpdateTime(LocalDateTime.now());
        reviewMeetingService.save(reviewMeeting);
        log.info("新增会议纪要成功，ID：{}", reviewMeeting.getId());
        return Result.success();
    }

    /**
     * 更新会议纪要
     */
    @PutMapping("/update")
    public Result<Void> update(@RequestBody ReviewMeeting reviewMeeting) {
        log.info("收到更新会议纪要请求：{}", reviewMeeting);
        reviewMeeting.setUpdateTime(LocalDateTime.now());
        reviewMeetingService.updateById(reviewMeeting);
        log.info("更新会议纪要成功，ID：{}", reviewMeeting.getId());
        return Result.success();
    }

    /**
     * 删除会议纪要
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("收到删除会议纪要请求，ID：{}", id);
        reviewMeetingService.removeById(id);
        log.info("删除会议纪要成功，ID：{}", id);
        return Result.success();
    }

    /**
     * 导出Excel
     */
    @GetMapping("/export")
    public void export(ReviewMeetingQueryDTO queryDTO, HttpServletResponse response) {
        log.info("收到导出评审会议纪要请求，条件：{}", queryDTO);
        reviewMeetingService.exportExcel(queryDTO, response);
    }

}
