package com.jscm.yxtyg.vo;

import lombok.Data;

/**
 * 相似工单
 */
@Data
public class SimilarTicketVO {

    private String orderNo;

    private String handlerName;

    private String city;

    private String status;

    private String handleUnit;

    private String contentSummary;

    private String solutionSummary;

    private Double similarityScore;
}
