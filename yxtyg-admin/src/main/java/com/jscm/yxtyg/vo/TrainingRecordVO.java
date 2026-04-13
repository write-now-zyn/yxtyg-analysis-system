package com.jscm.yxtyg.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 培训记录VO
 */
@Data
public class TrainingRecordVO {

    private Long id;

    private Long reportId;

    private LocalDateTime trainingTime;

    private String organizer;

    private String trainingSubject;

    private Integer coverageCount;

    private String trainingContent;

    private Boolean hasScreenshot;

    private String screenshotUrl;

    private String screenshotName;

    private LocalDateTime createTime;
}
