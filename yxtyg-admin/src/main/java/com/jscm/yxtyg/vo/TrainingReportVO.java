package com.jscm.yxtyg.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 转培上报VO
 */
@Data
public class TrainingReportVO {

    private Long id;

    private String city;

    /** 培训月份 */
    private String reportMonth;

    /** 上报时间 */
    private LocalDate reportTime;

    /** 上报人 */
    private String reporter;

    private Integer recordCount;

    /** 培训人数 */
    private Integer trainingPersonCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /** 培训记录列表（详情时使用） */
    private List<TrainingRecordVO> records;
}
