package com.jscm.yxtyg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jscm.yxtyg.common.PageResult;
import com.jscm.yxtyg.dto.ReviewMeetingQueryDTO;
import com.jscm.yxtyg.entity.ReviewMeeting;

import javax.servlet.http.HttpServletResponse;

/**
 * 评审会议纪要服务接口
 */
public interface ReviewMeetingService extends IService<ReviewMeeting> {

    /**
     * 分页查询会议纪要
     */
    PageResult<ReviewMeeting> queryPage(ReviewMeetingQueryDTO queryDTO);

    /**
     * 导出Excel
     */
    void exportExcel(ReviewMeetingQueryDTO queryDTO, HttpServletResponse response);

}
