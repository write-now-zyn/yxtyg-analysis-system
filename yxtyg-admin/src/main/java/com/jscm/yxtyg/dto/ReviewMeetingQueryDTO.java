package com.jscm.yxtyg.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 评审会议纪要查询DTO
 */
@Data
public class ReviewMeetingQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 会议时间开始 */
    private String meetingTimeStart;

    /** 会议时间结束 */
    private String meetingTimeEnd;

    /** 与会人员姓名 */
    private String participantName;

    /** 当前页 */
    private Long current = 1L;

    /** 每页大小 */
    private Long size = 10L;

}
