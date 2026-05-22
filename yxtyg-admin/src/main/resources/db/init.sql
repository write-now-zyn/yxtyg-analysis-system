-- =============================================
-- 一线体验官专项数据分析系统 - 数据库初始化脚本
-- 数据库：MySQL 8.0
-- 更新时间：2026-04-13
-- =============================================

SET NAMES utf8mb4;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS yxtyg_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 使用数据库
USE yxtyg_db;

-- =============================================
-- 1. 体验官表
-- =============================================
DROP TABLE IF EXISTS t_experimenter;
CREATE TABLE t_experimenter (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    city VARCHAR(20) NOT NULL COMMENT '地市',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    phone VARCHAR(20) DEFAULT NULL COMMENT '电话',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱（选填）',
    role VARCHAR(50) DEFAULT NULL COMMENT '角色（选填）',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    is_contact TINYINT DEFAULT 0 COMMENT '是否接口人(0-否,1-是)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_city (city),
    KEY idx_name (name),
    KEY idx_is_contact (is_contact)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体验官表';

-- =============================================
-- 2. 体验官操作日志表
-- =============================================
DROP TABLE IF EXISTS t_experimenter_log;
CREATE TABLE t_experimenter_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    operation_type VARCHAR(20) NOT NULL COMMENT '操作类型：新增、编辑、删除、批量新增、批量删除',
    operator VARCHAR(50) DEFAULT NULL COMMENT '操作人',
    operation_time DATETIME NOT NULL COMMENT '操作时间',
    experimenter_id BIGINT DEFAULT NULL COMMENT '体验官ID',
    city VARCHAR(50) DEFAULT NULL COMMENT '地市',
    name VARCHAR(50) DEFAULT NULL COMMENT '姓名',
    phone VARCHAR(20) DEFAULT NULL COMMENT '电话',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    role VARCHAR(100) DEFAULT NULL COMMENT '角色',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    is_contact TINYINT(1) DEFAULT 0 COMMENT '是否接口人 0-否 1-是',
    detail VARCHAR(500) DEFAULT NULL COMMENT '操作详情',
    PRIMARY KEY (id),
    KEY idx_operation_type (operation_type),
    KEY idx_operation_time (operation_time),
    KEY idx_experimenter_id (experimenter_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体验官操作日志表';

-- =============================================
-- 3. 工单主表
-- =============================================
DROP TABLE IF EXISTS t_work_order;
CREATE TABLE t_work_order (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    order_no VARCHAR(50) NOT NULL COMMENT 'BOMC流水号',
    declare_phone VARCHAR(20) DEFAULT NULL COMMENT '申告电话',
    handle_unit VARCHAR(100) DEFAULT NULL COMMENT '处理单位',
    handler_name VARCHAR(50) DEFAULT NULL COMMENT '处理人',
    city VARCHAR(50) DEFAULT NULL COMMENT '申告地市',
    create_time_str VARCHAR(50) DEFAULT NULL COMMENT '创建时间（原始字符串）',
    initiator_name VARCHAR(50) DEFAULT NULL COMMENT '发起人姓名',
    handle_satisfaction_score VARCHAR(20) DEFAULT NULL COMMENT '处理满意度评分',
    dispatch_satisfaction_score VARCHAR(20) DEFAULT NULL COMMENT '派单满意度评分',
    unsatisfied_reason VARCHAR(500) DEFAULT NULL COMMENT '不满意原因',
    status VARCHAR(50) DEFAULT NULL COMMENT '状态',
    solution_history_str TEXT COMMENT '解决方案历史原始字符串',
    content TEXT COMMENT '工单内容',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_status (order_no, status),
    KEY idx_order_no (order_no),
    KEY idx_city (city),
    KEY idx_status (status),
    KEY idx_create_time (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单主表';

-- =============================================
-- 4. 解决方案历史表
-- =============================================
DROP TABLE IF EXISTS t_solution_history;
CREATE TABLE t_solution_history (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    work_order_id BIGINT NOT NULL COMMENT '工单ID',
    order_no VARCHAR(50) NOT NULL COMMENT 'BOMC流水号（冗余存储）',
    handler_name VARCHAR(50) DEFAULT NULL COMMENT '处理人',
    opinion_content TEXT COMMENT '意见内容',
    sort_order INT DEFAULT 0 COMMENT '排序序号',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_work_order_id (work_order_id),
    KEY idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='解决方案历史表';

-- =============================================
-- 5. 评审会议纪要表
-- =============================================
DROP TABLE IF EXISTS t_review_meeting;
CREATE TABLE t_review_meeting (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    demand_no VARCHAR(50) NOT NULL COMMENT '评审方案需求号',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    meeting_time DATETIME NOT NULL COMMENT '会议时间',
    participants JSON DEFAULT NULL COMMENT '与会人列表（JSON: [{name,city}]）',
    content TEXT COMMENT '会议内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_meeting_time (meeting_time),
    KEY idx_demand_no (demand_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评审会议纪要表';

-- =============================================
-- 6. 转培上报记录表
-- =============================================
DROP TABLE IF EXISTS t_training_report;
CREATE TABLE t_training_report (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    city VARCHAR(20) NOT NULL COMMENT '地市',
    report_month VARCHAR(10) NOT NULL COMMENT '培训月份 YYYY-MM',
    report_time DATE DEFAULT NULL COMMENT '上报时间',
    reporter VARCHAR(50) DEFAULT NULL COMMENT '上报人',
    record_count INT DEFAULT 0 COMMENT '培训记录条数',
    training_person_count INT DEFAULT 0 COMMENT '培训人数（覆盖人数合计）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_city_month (city, report_month),
    KEY idx_city (city),
    KEY idx_report_month (report_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='转培上报记录表';

-- =============================================
-- 7. 培训记录明细表
-- =============================================
DROP TABLE IF EXISTS t_training_record;
CREATE TABLE t_training_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    report_id BIGINT NOT NULL COMMENT '关联上报记录ID',
    training_time DATETIME DEFAULT NULL COMMENT '培训时间',
    organizer VARCHAR(50) DEFAULT NULL COMMENT '组织人员',
    training_subject VARCHAR(200) DEFAULT NULL COMMENT '培训主体',
    coverage_count INT DEFAULT NULL COMMENT '覆盖人数',
    training_content TEXT COMMENT '培训内容',
    screenshot LONGBLOB COMMENT '培训截图（二进制存储）',
    screenshot_name VARCHAR(100) DEFAULT NULL COMMENT '图片文件名',
    screenshot_type VARCHAR(50) DEFAULT NULL COMMENT '图片类型(image/jpeg等)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_report_id (report_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='培训记录明细表';

-- =============================================
-- 8. 系统角色表
-- =============================================
DROP TABLE IF EXISTS t_sys_role;
CREATE TABLE t_sys_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    code VARCHAR(50) NOT NULL COMMENT '角色编码',
    name VARCHAR(50) NOT NULL COMMENT '角色名称',
    description VARCHAR(200) DEFAULT NULL COMMENT '角色说明',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

-- =============================================
-- 9. 系统权限表
-- =============================================
DROP TABLE IF EXISTS t_sys_permission;
CREATE TABLE t_sys_permission (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    code VARCHAR(80) NOT NULL COMMENT '权限编码',
    name VARCHAR(80) NOT NULL COMMENT '权限名称',
    description VARCHAR(200) DEFAULT NULL COMMENT '权限说明',
    PRIMARY KEY (id),
    UNIQUE KEY uk_permission_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统权限表';

-- =============================================
-- 10. 角色权限关联表
-- =============================================
DROP TABLE IF EXISTS t_sys_role_permission;
CREATE TABLE t_sys_role_permission (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    permission_code VARCHAR(80) NOT NULL COMMENT '权限编码',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_permission (role_code, permission_code),
    KEY idx_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- =============================================
-- 11. 系统用户表
-- =============================================
DROP TABLE IF EXISTS t_sys_user;
CREATE TABLE t_sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '登录用户名',
    password VARCHAR(100) NOT NULL COMMENT 'BCrypt密码',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    phone VARCHAR(30) DEFAULT NULL COMMENT '电话',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    status TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_role_code (role_code),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- =============================================
-- 12. 登录会话表
-- =============================================
DROP TABLE IF EXISTS t_sys_session;
CREATE TABLE t_sys_session (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    token VARCHAR(64) NOT NULL COMMENT '访问令牌',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    expires_at DATETIME NOT NULL COMMENT '过期时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_token (token),
    KEY idx_user_id (user_id),
    KEY idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录会话表';

-- =============================================
-- 13. 工作量核定需求表
-- =============================================
DROP TABLE IF EXISTS t_demand_workload;
CREATE TABLE t_demand_workload (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    demand_no VARCHAR(50) NOT NULL COMMENT '需求编号',
    demand_name VARCHAR(200) NOT NULL COMMENT '需求名称',
    demand_description TEXT COMMENT '需求描述',
    product_manager_id BIGINT NOT NULL COMMENT '产品经理用户ID',
    product_manager_name VARCHAR(50) NOT NULL COMMENT '产品经理姓名',
    system_name VARCHAR(100) NOT NULL COMMENT '归属系统',
    initial_workload DECIMAL(10,2) NOT NULL COMMENT '初核工作量',
    initial_amount DECIMAL(12,2) DEFAULT 0 COMMENT '初核金额',
    final_workload DECIMAL(10,2) DEFAULT NULL COMMENT '最终核定工作量',
    reduction_workload DECIMAL(10,2) DEFAULT 0 COMMENT '核减工作量',
    status VARCHAR(20) NOT NULL DEFAULT '待填写' COMMENT '需求状态：待填写、已填写、已核定',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_demand_no (demand_no),
    KEY idx_product_manager (product_manager_id),
    KEY idx_system_name (system_name),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作量核定需求表';

-- =============================================
-- 14. 工作量催办记录表
-- =============================================
DROP TABLE IF EXISTS t_demand_reminder;
CREATE TABLE t_demand_reminder (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    demand_id BIGINT NOT NULL COMMENT '需求ID',
    reminder_content VARCHAR(500) DEFAULT NULL COMMENT '催办内容',
    reminder_by BIGINT NOT NULL COMMENT '催办人用户ID',
    reminder_to BIGINT NOT NULL COMMENT '被催办人用户ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '催办时间',
    PRIMARY KEY (id),
    KEY idx_demand_id (demand_id),
    KEY idx_reminder_to (reminder_to)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作量催办记录表';

-- =============================================
-- 15. 系统通知表
-- =============================================
DROP TABLE IF EXISTS t_notification;
CREATE TABLE t_notification (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    recipient_user_id BIGINT NOT NULL COMMENT '接收人用户ID',
    title VARCHAR(100) NOT NULL COMMENT '通知标题',
    content VARCHAR(500) NOT NULL COMMENT '通知内容',
    biz_type VARCHAR(50) DEFAULT NULL COMMENT '业务类型',
    biz_id BIGINT DEFAULT NULL COMMENT '业务ID',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读 0-否 1-是',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    read_at DATETIME DEFAULT NULL COMMENT '阅读时间',
    PRIMARY KEY (id),
    KEY idx_recipient_read (recipient_user_id, is_read),
    KEY idx_biz (biz_type, biz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统通知表';

-- =============================================
-- 初始化用户、角色、权限和工作量样例数据
-- 默认密码均为：123456
-- =============================================
INSERT INTO t_sys_role (code, name, description) VALUES
('SYSTEM_ADMIN', '系统管理员', '管理用户、角色、权限和系统配置'),
('DEV_ADMIN', '开发管理员', '导入需求、核定工作量、催办产品经理'),
('PRODUCT_MANAGER', '产品经理', '查看本人需求并填写最终核定工作量');

INSERT INTO t_sys_permission (code, name, description) VALUES
('demand:view', '查看工作量需求', '查看工作量核定需求列表和详情'),
('demand:manage', '管理工作量需求', '新增、编辑、删除、导入和导出工作量需求'),
('demand:fill', '填写最终工作量', '产品经理填写最终核定工作量'),
('demand:confirm', '核定工作量', '开发管理员确认工作量核定结果'),
('demand:remind', '催办需求', '对待填写需求发起催办'),
('user:manage', '用户管理', '维护系统用户'),
('role:manage', '角色权限管理', '维护角色权限配置'),
('notification:view', '查看通知', '查看本人通知');

INSERT INTO t_sys_role_permission (role_code, permission_code) VALUES
('SYSTEM_ADMIN', 'demand:view'), ('SYSTEM_ADMIN', 'demand:manage'), ('SYSTEM_ADMIN', 'demand:fill'), ('SYSTEM_ADMIN', 'demand:confirm'), ('SYSTEM_ADMIN', 'demand:remind'), ('SYSTEM_ADMIN', 'user:manage'), ('SYSTEM_ADMIN', 'role:manage'), ('SYSTEM_ADMIN', 'notification:view'),
('DEV_ADMIN', 'demand:view'), ('DEV_ADMIN', 'demand:manage'), ('DEV_ADMIN', 'demand:confirm'), ('DEV_ADMIN', 'demand:remind'), ('DEV_ADMIN', 'notification:view'),
('PRODUCT_MANAGER', 'demand:view'), ('PRODUCT_MANAGER', 'demand:fill'), ('PRODUCT_MANAGER', 'notification:view');

INSERT INTO t_sys_user (username, password, name, role_code, phone, email, status) VALUES
('admin', '$2a$10$n63oMzDv8yCtAQVreNZlOuQUCjdAyyrHl2gEPPXbVDWEL7hDrros6', '系统管理员', 'SYSTEM_ADMIN', '13800000000', 'admin@example.com', 1),
('devadmin', '$2a$10$n63oMzDv8yCtAQVreNZlOuQUCjdAyyrHl2gEPPXbVDWEL7hDrros6', '开发管理员', 'DEV_ADMIN', '13800000001', 'devadmin@example.com', 1),
('pm_zhang', '$2a$10$n63oMzDv8yCtAQVreNZlOuQUCjdAyyrHl2gEPPXbVDWEL7hDrros6', '张产品', 'PRODUCT_MANAGER', '13800000002', 'pm_zhang@example.com', 1),
('pm_li', '$2a$10$n63oMzDv8yCtAQVreNZlOuQUCjdAyyrHl2gEPPXbVDWEL7hDrros6', '李产品', 'PRODUCT_MANAGER', '13800000003', 'pm_li@example.com', 1);

INSERT INTO t_demand_workload (demand_no, demand_name, demand_description, product_manager_id, product_manager_name, system_name, initial_workload, initial_amount, final_workload, reduction_workload, status, remark) VALUES
('REQ-202605-001', '智能分单规则优化', '优化需求提单的智能分单策略，提升推荐准确率。', 3, '张产品', '一线体验官平台', 12.00, 9600.00, NULL, 0.00, '待填写', NULL),
('REQ-202605-002', '转培上报导出增强', '增加转培数据按地市和月份聚合导出能力。', 4, '李产品', '培训管理模块', 8.00, 6400.00, 6.50, 1.50, '已填写', '已完成产品侧填写'),
('REQ-202605-003', '体验官画像字段补齐', '补充体验官角色和接口人维度的数据维护能力。', 3, '张产品', '体验官管理模块', 5.00, 4000.00, 5.00, 0.00, '已核定', '无需核减');

-- =============================================
-- 16. 大模型配置表
-- =============================================
DROP TABLE IF EXISTS t_model_config;
CREATE TABLE t_model_config (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(50) NOT NULL COMMENT '配置名称（别名）',
    provider VARCHAR(50) NOT NULL COMMENT '提供商类型（ollama/openai/custom）',
    base_url VARCHAR(255) NOT NULL COMMENT 'API基础地址',
    api_key VARCHAR(255) DEFAULT NULL COMMENT 'API密钥（加密存储）',
    models TEXT COMMENT '可用模型列表（JSON）',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认模型（0-否,1-是）',
    status TINYINT DEFAULT 1 COMMENT '状态（0-禁用,1-启用）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_provider (provider),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='大模型配置表';

-- =============================================
-- 17. 向量化模型配置表
-- =============================================
DROP TABLE IF EXISTS t_embedding_config;
CREATE TABLE t_embedding_config (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(50) NOT NULL COMMENT '配置名称（别名）',
    provider VARCHAR(50) NOT NULL COMMENT '提供商类型（ollama/openai/custom）',
    base_url VARCHAR(255) NOT NULL COMMENT 'Embedding接口地址',
    api_key VARCHAR(255) DEFAULT NULL COMMENT 'API密钥',
    model VARCHAR(100) NOT NULL COMMENT 'Embedding模型名称',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认配置（0-否,1-是）',
    status TINYINT DEFAULT 1 COMMENT '状态（0-禁用,1-启用）',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_provider (provider),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='向量化模型配置表';

-- =============================================
-- 18. 智能体配置表
-- =============================================
DROP TABLE IF EXISTS t_agent_config;
CREATE TABLE t_agent_config (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    agent_code VARCHAR(50) NOT NULL COMMENT '智能体编码（唯一标识）',
    agent_name VARCHAR(100) NOT NULL COMMENT '智能体名称',
    description VARCHAR(500) DEFAULT NULL COMMENT '功能描述',
    model_config_id BIGINT DEFAULT NULL COMMENT '关联的大模型配置ID',
    model_name VARCHAR(100) DEFAULT NULL COMMENT '使用的具体模型名称',
    temperature DECIMAL(3,2) DEFAULT 0.70 COMMENT '温度参数（0-2）',
    top_p DECIMAL(3,2) DEFAULT 0.90 COMMENT 'Top P参数（0-1）',
    repetition_penalty DECIMAL(3,2) DEFAULT 1.10 COMMENT '重复惩罚（0-2）',
    system_prompt TEXT COMMENT '系统提示词',
    user_prompt TEXT COMMENT '用户提示词模板，使用{content}占位符',
    max_tokens INT DEFAULT 2048 COMMENT '最大输出token数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_agent_code (agent_code),
    KEY idx_model_config_id (model_config_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能体配置表';

-- =============================================
-- 初始化智能体数据
-- =============================================
INSERT INTO t_agent_config (agent_code, agent_name, description, temperature, top_p, repetition_penalty, system_prompt, user_prompt, max_tokens) VALUES
-- 与会人员识别智能体
('participant_parser', '与会人员识别', '从签到文本中识别与会人员的姓名和地市', 0.10, 0.10, 1.00, 
'从文本中提取与会人员的姓名和地市。
江苏省地市：南京、苏州、无锡、常州、镇江、扬州、泰州、南通、盐城、淮安、连云港、徐州、宿迁
输入格式通常是"地市+姓名"，如"苏州任丽华"表示地市=苏州，姓名=任丽华，任何空格或非中文都视为连接符
每一行是一次签到，如果没有地市前缀，city字段留空
只输出JSON数组：[{"name":"姓名","city":"地市"}]', 
'{content}', 2048),

-- 转培记录识别智能体
('training_parser', '转培记录识别', '从表格文本中提取培训记录信息', 0.00, 0.10, 1.00,
'从表格文本中提取培训记录信息。
江苏省地市：南京、苏州、无锡、常州、镇江、扬州、泰州、南通、盐城、淮安、连云港、徐州、宿迁
每一行都是一次培训记录，不要遗漏数据，也不要过度思考
字段映射规则（第一行为表头）：
- 地市（可能叫"地市"或"归属"）-> city
- 组织人员 -> organizer
- 辅导/培训主题或主体 -> trainingContent
- 辅导/培训时间 -> trainingTime
- 覆盖人数 -> coverageCount
时间格式处理：
- 转换为当年日期，格式为yyyy-MM-dd
只输出JSON数组：[{"city":"","organizer":"","trainingContent":"","trainingTime":"","coverageCount":""}]',
'{content}', 2048);

-- =============================================
-- MySQL 配置建议
-- =============================================

-- 修复 MySQL max_allowed_packet 限制（解决上传大图片时报错）
-- SET GLOBAL max_allowed_packet = 67108864;  -- 64MB

-- =============================================
-- 脚本执行完成
-- =============================================
SELECT '数据库初始化完成' AS result;
