package com.jscm.yxtyg.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 智能推荐结果
 */
@Data
public class RecommendResultVO {

    private String reason;

    private List<RecommendPersonVO> recommendPersons = new ArrayList<>();

    private List<SimilarTicketVO> similarTickets = new ArrayList<>();
}
