package com.jscm.yxtyg.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 工单详情VO
 * 展示全部字段，包含解决方案历史列表
 */
@Data
public class WorkOrderDetailVO {

    /** 主键ID */
    private Long id;

    /** 问题编号（流水号） */
    private String orderNo;

    /** 申告电话 */
    private String declarePhone;

    /** 处理单位 */
    private String handleUnit;

    /** 处理人姓名 */
    private String handlerName;

    /** 地市 */
    private String city;

    /** 创建时间（原始字符串） */
    private String createTimeStr;

    /** 发起人姓名 */
    private String initiatorName;

    /** 处理满意度评分 */
    private String handleSatisfactionScore;

    /** 派单满意度评分 */
    private String dispatchSatisfactionScore;

    /** 不满意原因 */
    private String unsatisfiedReason;

    /** 状态 */
    private String status;

    /** 解决方案历史原始字符串 */
    private String solutionHistoryStr;

    /** 工单内容 */
    private String content;

    /** 解决方案历史列表 */
    private List<SolutionHistoryVO> solutionHistoryList;

    /** 记录创建时间 */
    private LocalDateTime createdAt;

    /** 记录更新时间 */
    private LocalDateTime updatedAt;

    /**
     * 解决方案历史子对象
     */
    @Data
    public static class SolutionHistoryVO {
        /** 主键ID */
        private Long id;

        /** 处理人姓名 */
        private String handlerName;

        /** 意见内容 */
        private String opinionContent;

        /** 排序序号 */
        private Integer sortOrder;
    }
}
