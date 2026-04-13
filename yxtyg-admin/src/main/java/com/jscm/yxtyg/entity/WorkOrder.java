package com.jscm.yxtyg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 工单实体类
 */
@Data
@TableName("t_work_order")
public class WorkOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** BOMC流水号 */
    private String orderNo;

    /** 申告电话 */
    private String declarePhone;

    /** 处理单位 */
    private String handleUnit;

    /** 处理人 */
    private String handlerName;

    /** 申告地市 */
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

    /** 记录创建时间 */
    private LocalDateTime createdAt;

    /** 记录更新时间 */
    private LocalDateTime updatedAt;
}
