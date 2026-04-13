package com.jscm.yxtyg.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 培训记录DTO
 */
@Data
public class TrainingRecordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 培训时间 */
    private String trainingTime;

    /** 组织人员 */
    private String organizer;

    /** 培训主体 */
    private String trainingSubject;

    /** 覆盖人数 */
    private Integer coverageCount;

    /** 培训内容 */
    private String trainingContent;

    /** 培训截图Base64 */
    private String screenshotBase64;

    /** 图片文件名 */
    private String screenshotName;

    /** 图片类型 */
    private String screenshotType;

    /** 是否有截图 */
    private Boolean hasScreenshot;

}
