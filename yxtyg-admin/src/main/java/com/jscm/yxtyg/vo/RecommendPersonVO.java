package com.jscm.yxtyg.vo;

import lombok.Data;

/**
 * 推荐处理人
 */
@Data
public class RecommendPersonVO {

    private String name;

    private Double confidence;

    private String reason;

    private Integer ticketCount;
}
