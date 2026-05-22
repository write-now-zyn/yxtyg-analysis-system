package com.jscm.yxtyg.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DemandWorkloadExcelDTO {

    @ExcelProperty("需求编号")
    @ColumnWidth(18)
    private String demandNo;

    @ExcelProperty("需求名称")
    @ColumnWidth(28)
    private String demandName;

    @ExcelProperty("需求描述")
    @ColumnWidth(40)
    private String demandDescription;

    @ExcelProperty("产品经理用户名")
    @ColumnWidth(18)
    private String productManagerUsername;

    @ExcelProperty("产品经理")
    @ColumnWidth(14)
    private String productManagerName;

    @ExcelProperty("归属系统")
    @ColumnWidth(18)
    private String systemName;

    @ExcelProperty("初核工作量")
    @ColumnWidth(14)
    private BigDecimal initialWorkload;

    @ExcelProperty("初核金额")
    @ColumnWidth(14)
    private BigDecimal initialAmount;

    @ExcelProperty("最终核定工作量")
    @ColumnWidth(16)
    private BigDecimal finalWorkload;

    @ExcelProperty("需求状态")
    @ColumnWidth(12)
    private String status;

    @ExcelProperty("备注")
    @ColumnWidth(30)
    private String remark;
}
