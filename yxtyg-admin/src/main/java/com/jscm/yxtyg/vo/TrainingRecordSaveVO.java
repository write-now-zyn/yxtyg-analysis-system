package com.jscm.yxtyg.vo;

import lombok.Data;

/**
 * 培训记录保存VO（用于接收前端数据）
 */
@Data
public class TrainingRecordSaveVO {

    /**
     * 培训时间
     */
    private String trainingTime;

    /**
     * 组织人员
     */
    private String organizer;

    /**
     * 培训主体
     */
    private String trainingSubject;

    /**
     * 覆盖人数
     */
    private Integer coverageCount;

    /**
     * 培训内容
     */
    private String trainingContent;

    /**
     * 培训截图（Base64编码）
     */
    private String screenshotBase64;

    /**
     * 图片文件名
     */
    private String screenshotName;

    /**
     * 图片类型
     */
    private String screenshotType;
}
