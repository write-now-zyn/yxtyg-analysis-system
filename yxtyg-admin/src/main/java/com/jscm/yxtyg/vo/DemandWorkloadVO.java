package com.jscm.yxtyg.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DemandWorkloadVO {

    private Long id;
    private String demandNo;
    private String demandName;
    private String demandDescription;
    private Long productManagerId;
    private String productManagerName;
    private String systemName;
    private BigDecimal initialWorkload;
    private BigDecimal initialAmount;
    private BigDecimal finalWorkload;
    private BigDecimal reductionWorkload;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<DemandReminderVO> reminders;
}
