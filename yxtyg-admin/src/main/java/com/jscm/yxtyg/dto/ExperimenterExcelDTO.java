package com.jscm.yxtyg.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import lombok.Data;

/**
 * 体验官Excel导入导出DTO
 */
@Data
@HeadRowHeight(20)  // 标题行高度
@ContentRowHeight(18)  // 内容行高度
@HeadStyle(fillForegroundColor = 44)  // 移动蓝背景色
public class ExperimenterExcelDTO {

    @ExcelProperty("地市")
    @ColumnWidth(12)
    private String city;

    @ExcelProperty("姓名")
    @ColumnWidth(12)
    private String name;

    @ExcelProperty("电话")
    @ColumnWidth(15)
    private String phone;

    @ExcelProperty("邮箱")
    @ColumnWidth(25)
    private String email;

    @ExcelProperty("角色")
    @ColumnWidth(15)
    private String role;

    @ExcelProperty("备注")
    @ColumnWidth(30)
    private String remark;
}
