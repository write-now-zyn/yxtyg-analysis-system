package com.jscm.yxtyg.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DemandWorkloadStatisticsVO {

    private Long total;
    private Long pendingCount;
    private Long filledCount;
    private Long confirmedCount;
    private BigDecimal initialWorkloadTotal;
    private BigDecimal finalWorkloadTotal;
    private BigDecimal reductionWorkloadTotal;
}
