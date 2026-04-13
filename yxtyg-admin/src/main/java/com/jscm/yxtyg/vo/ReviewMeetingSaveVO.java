package com.jscm.yxtyg.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 评审会议纪要保存VO
 */
@Data
public class ReviewMeetingSaveVO {

    /**
     * ID（更新时传入）
     */
    private Long id;

    /**
     * 评审方案需求号
     */
    @NotBlank(message = "需求号不能为空")
    private String demandNo;

    /**
     * 标题
     */
    @NotBlank(message = "标题不能为空")
    private String title;

    /**
     * 会议时间
     */
    @NotNull(message = "会议时间不能为空")
    private String meetingTime;

    /**
     * 与会人列表
     */
    @NotNull(message = "与会人列表不能为空")
    private List<ParticipantVO> participants;

    /**
     * 会议内容
     */
    private String content;
}
