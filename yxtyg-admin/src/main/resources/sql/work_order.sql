-- ==========================================
-- 工单模块数据库表
-- 创建时间：2026-03-20
-- ==========================================

-- ----------------------------
-- 1. 工单主表
-- Excel表头：BOMC流水号、申告号码、处理单位、处理人、申告地市、创建时间、
--          发起人姓名、处理满意度评分、派单满意度评分、不满意原因、
--          状态、解决方案历史、工单内容
-- ----------------------------
DROP TABLE IF EXISTS `t_work_order`;
CREATE TABLE `t_work_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT 'BOMC流水号',
    `declare_phone` VARCHAR(20) DEFAULT NULL COMMENT '申告电话',
    `handle_unit` VARCHAR(100) DEFAULT NULL COMMENT '处理单位',
    `handler_name` VARCHAR(50) DEFAULT NULL COMMENT '处理人',
    `city` VARCHAR(50) DEFAULT NULL COMMENT '申告地市',
    `create_time_str` VARCHAR(50) DEFAULT NULL COMMENT '创建时间（原始字符串）',
    `initiator_name` VARCHAR(50) DEFAULT NULL COMMENT '发起人姓名',
    `handle_satisfaction_score` VARCHAR(20) DEFAULT NULL COMMENT '处理满意度评分',
    `dispatch_satisfaction_score` VARCHAR(20) DEFAULT NULL COMMENT '派单满意度评分',
    `unsatisfied_reason` VARCHAR(500) DEFAULT NULL COMMENT '不满意原因',
    `status` VARCHAR(50) DEFAULT NULL COMMENT '状态',
    `solution_history_str` TEXT DEFAULT NULL COMMENT '解决方案历史原始字符串',
    `content` TEXT DEFAULT NULL COMMENT '工单内容',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_status` (`order_no`, `status`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_city` (`city`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单主表';

-- ----------------------------
-- 2. 解决方案历史表
-- ----------------------------
DROP TABLE IF EXISTS `t_solution_history`;
CREATE TABLE `t_solution_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `work_order_id` BIGINT NOT NULL COMMENT '工单ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT 'BOMC流水号（冗余存储）',
    `handler_name` VARCHAR(50) DEFAULT NULL COMMENT '处理人',
    `opinion_content` TEXT DEFAULT NULL COMMENT '意见内容',
    `sort_order` INT DEFAULT 0 COMMENT '排序序号',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_work_order_id` (`work_order_id`),
    KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='解决方案历史表';
