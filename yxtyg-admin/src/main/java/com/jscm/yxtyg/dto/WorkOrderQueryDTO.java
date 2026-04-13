package com.jscm.yxtyg.dto;

import lombok.Data;

/**
 * 工单查询DTO
 */
@Data
public class WorkOrderQueryDTO {

    /** 问题编号 */
    private String orderNo;

    /** 发起人姓名 */
    private String initiatorName;

    /** 地市 */
    private String city;

    /** 月份（格式：yyyyMM） */
    private String month;

    /** 状态 */
    private String status;

    /** 关键词（模糊搜索工单内容） */
    private String keyword;

    /** 当前页码 */
    private Integer current = 1;

    /** 每页条数 */
    private Integer size = 10;
}
