package com.jscm.yxtyg.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 转培上报查询DTO
 */
@Data
public class TrainingReportQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 地市 */
    private String city;

    /** 上报月份 */
    private String reportMonth;

    /** 培训内容关键词 */
    private String trainingContent;

    /** 培训主体关键词 */
    private String trainingSubject;

    /** 当前页 */
    private Long current = 1L;

    /** 每页大小 */
    private Long size = 10L;

}
