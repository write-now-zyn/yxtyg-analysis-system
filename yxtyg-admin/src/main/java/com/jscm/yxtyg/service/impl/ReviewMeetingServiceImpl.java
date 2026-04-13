package com.jscm.yxtyg.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jscm.yxtyg.common.PageResult;
import com.jscm.yxtyg.dto.ReviewMeetingQueryDTO;
import com.jscm.yxtyg.entity.ReviewMeeting;
import com.jscm.yxtyg.mapper.ReviewMeetingMapper;
import com.jscm.yxtyg.service.ReviewMeetingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * 评审会议纪要服务实现类
 */
@Slf4j
@Service
public class ReviewMeetingServiceImpl extends ServiceImpl<ReviewMeetingMapper, ReviewMeeting> implements ReviewMeetingService {

    @Override
    public PageResult<ReviewMeeting> queryPage(ReviewMeetingQueryDTO queryDTO) {
        log.debug("查询评审会议纪要列表，条件：{}", queryDTO);
        
        LambdaQueryWrapper<ReviewMeeting> wrapper = new LambdaQueryWrapper<>();
        
        // 会议时间范围查询
        if (StringUtils.hasText(queryDTO.getMeetingTimeStart())) {
            wrapper.ge(ReviewMeeting::getMeetingTime, queryDTO.getMeetingTimeStart() + " 00:00:00");
        }
        if (StringUtils.hasText(queryDTO.getMeetingTimeEnd())) {
            wrapper.le(ReviewMeeting::getMeetingTime, queryDTO.getMeetingTimeEnd() + " 23:59:59");
        }
        
        // 与会人员姓名模糊查询（JSON字段查询）
        if (StringUtils.hasText(queryDTO.getParticipantName())) {
            wrapper.like(ReviewMeeting::getParticipants, queryDTO.getParticipantName());
        }
        
        wrapper.orderByDesc(ReviewMeeting::getMeetingTime);
        
        Page<ReviewMeeting> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<ReviewMeeting> result = this.page(page, wrapper);
        
        log.info("查询评审会议纪要列表完成，共{}条记录", result.getTotal());
        return PageResult.of(result);
    }

    @Override
    public void exportExcel(ReviewMeetingQueryDTO queryDTO, HttpServletResponse response) {
        log.info("开始导出评审会议纪要Excel，条件：{}", queryDTO);
        
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("评审会议纪要", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            PageResult<ReviewMeeting> pageResult = queryPage(queryDTO);
            
            EasyExcel.write(response.getOutputStream(), ReviewMeeting.class)
                    .sheet("评审会议纪要")
                    .doWrite(pageResult.getRecords());
            
            log.info("导出评审会议纪要完成，共{}条记录", pageResult.getTotal());
        } catch (IOException e) {
            log.error("导出评审会议纪要Excel失败", e);
            throw new RuntimeException("导出失败", e);
        }
    }

}
