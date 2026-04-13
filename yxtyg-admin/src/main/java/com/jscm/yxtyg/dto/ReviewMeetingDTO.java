package com.jscm.yxtyg.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评审会议纪要DTO
 */
@Data
public class ReviewMeetingDTO {

    private Long id;

    @NotBlank(message = "评审方案需求号不能为空")
    private String demandNo;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotNull(message = "会议时间不能为空")
    private LocalDateTime meetingTime;

    @NotNull(message = "与会人列表不能为空")
    private List<ParticipantDTO> participants;

    private String content;
}
