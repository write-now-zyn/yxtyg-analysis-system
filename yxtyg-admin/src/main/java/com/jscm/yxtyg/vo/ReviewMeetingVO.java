package com.jscm.yxtyg.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评审会议纪要VO
 */
@Data
public class ReviewMeetingVO {

    private Long id;

    private String demandNo;

    private String title;

    private LocalDateTime meetingTime;

    private List<ParticipantVO> participants;

    private String content;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
