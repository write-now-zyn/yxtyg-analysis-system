package com.jscm.yxtyg.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import lombok.Data;

/**
 * 工单Excel导入DTO
 * 用于映射Excel表格中的列字段
 * 
 * Excel表头：BOMC流水号、申告号码、处理单位、处理人、申告地市、创建时间、
 *          发起人姓名、处理满意度评分、派单满意度评分、不满意原因、
 *          状态、解决方案历史、工单内容
 */
@Data
@HeadRowHeight(20)
@ContentRowHeight(18)
@HeadStyle(fillForegroundColor = 44)
public class WorkOrderExcelDTO {

    @ExcelProperty("BOMC流水号")
    @ColumnWidth(20)
    private String orderNo;

    @ExcelProperty("申告号码")
    @ColumnWidth(20)
    private String declarePhone;

    @ExcelProperty("处理单位")
    @ColumnWidth(20)
    private String handleUnit;

    @ExcelProperty("处理人")
    @ColumnWidth(12)
    private String handlerName;

    @ExcelProperty("申告地市")
    @ColumnWidth(12)
    private String city;

    @ExcelProperty("创建时间")
    @ColumnWidth(20)
    private String createTimeStr;

    @ExcelProperty("发起人姓名")
    @ColumnWidth(12)
    private String initiatorName;

    @ExcelProperty("处理满意度评分")
    @ColumnWidth(15)
    private String handleSatisfactionScore;

    @ExcelProperty("派单满意度评分")
    @ColumnWidth(15)
    private String dispatchSatisfactionScore;

    @ExcelProperty("不满意原因")
    @ColumnWidth(30)
    private String unsatisfiedReason;

    @ExcelProperty("状态")
    @ColumnWidth(15)
    private String status;

    @ExcelProperty("解决方案历史")
    @ColumnWidth(50)
    private String solutionHistoryStr;

    @ExcelProperty("工单内容")
    @ColumnWidth(50)
    private String content;
}
