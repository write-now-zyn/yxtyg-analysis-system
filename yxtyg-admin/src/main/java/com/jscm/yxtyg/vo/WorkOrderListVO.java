package com.jscm.yxtyg.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工单列表VO
 * 只展示关键字段
 */
@Data
public class WorkOrderListVO {

    /** 主键ID */
    private Long id;

    /** 问题编号（流水号） */
    private String orderNo;

    /** 发起人姓名 */
    private String initiatorName;

    /** 地市 */
    private String city;

    /** 工单内容 */
    private String content;

    /** 状态 */
    private String status;

    /** 记录创建时间 */
    private LocalDateTime createdAt;
}
