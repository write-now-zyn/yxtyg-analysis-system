package com.jscm.yxtyg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_demand_workload")
public class DemandWorkload implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
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
}
