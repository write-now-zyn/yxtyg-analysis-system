package com.jscm.yxtyg.vector;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 工单向量记录
 */
@Data
public class WorkOrderVectorRecord {

    private String orderNo;

    private String contentHash;

    private String contentText;

    private String handlerName;

    private String solutionSummary;

    private String city;

    private String status;

    private String handleUnit;

    private List<Double> vector = new ArrayList<>();

    private String createdAt;

    private String updatedAt;
}
