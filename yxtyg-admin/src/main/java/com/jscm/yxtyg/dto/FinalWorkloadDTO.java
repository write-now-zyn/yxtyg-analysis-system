package com.jscm.yxtyg.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class FinalWorkloadDTO {

    @NotNull(message = "最终核定工作量不能为空")
    private BigDecimal finalWorkload;

    private String remark;
}
