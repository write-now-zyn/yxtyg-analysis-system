package com.jscm.yxtyg.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class DemandWorkloadDTO {

    private Long id;

    @NotBlank(message = "需求编号不能为空")
    private String demandNo;

    @NotBlank(message = "需求名称不能为空")
    private String demandName;

    private String demandDescription;

    @NotNull(message = "产品经理不能为空")
    private Long productManagerId;

    @NotBlank(message = "归属系统不能为空")
    private String systemName;

    @NotNull(message = "初核工作量不能为空")
    private BigDecimal initialWorkload;

    private BigDecimal initialAmount;
    private BigDecimal finalWorkload;
    private String status;
    private String remark;
}
