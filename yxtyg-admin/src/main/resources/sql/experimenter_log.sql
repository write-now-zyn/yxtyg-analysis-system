-- 体验官操作日志表
CREATE TABLE IF NOT EXISTS `t_experimenter_log` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `operation_type` VARCHAR(20) NOT NULL COMMENT '操作类型：新增、编辑、删除、批量新增、批量删除',
    `operator` VARCHAR(50) DEFAULT NULL COMMENT '操作人',
    `operation_time` DATETIME NOT NULL COMMENT '操作时间',
    `experimenter_id` BIGINT(20) DEFAULT NULL COMMENT '体验官ID',
    `city` VARCHAR(50) DEFAULT NULL COMMENT '地市',
    `name` VARCHAR(50) DEFAULT NULL COMMENT '姓名',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '电话',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `role` VARCHAR(100) DEFAULT NULL COMMENT '角色',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `is_contact` TINYINT(1) DEFAULT 0 COMMENT '是否接口人 0-否 1-是',
    `detail` VARCHAR(500) DEFAULT NULL COMMENT '操作详情',
    PRIMARY KEY (`id`),
    KEY `idx_operation_type` (`operation_type`),
    KEY `idx_operation_time` (`operation_time`),
    KEY `idx_experimenter_id` (`experimenter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体验官操作日志表';
