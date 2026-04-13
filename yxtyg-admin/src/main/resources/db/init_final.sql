-- =============================================
-- 一线体验官专项数据分析系统 - 数据库初始化脚本
-- 数据库：MySQL 8.0
-- 创建时间：2026-03-30
-- 版本：1.0.0
-- 说明：汇总自docs和db文件夹下的所有SQL脚本
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS yxtyg_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 使用数据库
USE yxtyg_db;

-- =============================================
-- 1. 体验官表
-- =============================================
DROP TABLE IF EXISTS t_experimenter;
CREATE TABLE t_experimenter (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    city VARCHAR(20) NOT NULL COMMENT '地市',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    phone VARCHAR(20) COMMENT '电话',
    email VARCHAR(100) COMMENT '邮箱（选填）',
    role VARCHAR(50) COMMENT '角色（选填）',
    remark VARCHAR(500) COMMENT '备注',
    is_contact TINYINT DEFAULT 0 COMMENT '是否接口人(0-否,1-是)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_city (city),
    INDEX idx_name (name),
    INDEX idx_is_contact (is_contact)
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
    solution_history_str TEXT DEFAULT NULL COMMENT '解决方案历史原始字符串',
    content TEXT DEFAULT NULL COMMENT '工单内容',
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
    opinion_content TEXT DEFAULT NULL COMMENT '意见内容',
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
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    demand_no VARCHAR(50) NOT NULL COMMENT '评审方案需求号',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    meeting_time DATETIME NOT NULL COMMENT '会议时间',
    participants JSON COMMENT '与会人列表（JSON: [{name,city}]）',
    content TEXT COMMENT '会议内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_meeting_time (meeting_time),
    INDEX idx_demand_no (demand_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评审会议纪要表';

-- =============================================
-- 6. 转培上报记录表
-- =============================================
DROP TABLE IF EXISTS t_training_report;
CREATE TABLE t_training_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    city VARCHAR(20) NOT NULL COMMENT '地市',
    report_month VARCHAR(10) NOT NULL COMMENT '培训月份 YYYY-MM',
    report_time DATE DEFAULT NULL COMMENT '上报时间',
    reporter VARCHAR(50) DEFAULT NULL COMMENT '上报人',
    record_count INT DEFAULT 0 COMMENT '培训记录条数',
    training_person_count INT DEFAULT 0 COMMENT '培训人数（覆盖人数合计）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_city_month (city, report_month),
    INDEX idx_city (city),
    INDEX idx_report_month (report_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='转培上报记录表';

-- =============================================
-- 7. 培训记录明细表
-- =============================================
DROP TABLE IF EXISTS t_training_record;
CREATE TABLE t_training_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    report_id BIGINT NOT NULL COMMENT '关联上报记录ID',
    training_time DATETIME COMMENT '培训时间',
    organizer VARCHAR(50) COMMENT '组织人员',
    training_subject VARCHAR(200) COMMENT '培训主体',
    coverage_count INT COMMENT '覆盖人数',
    training_content TEXT COMMENT '培训内容',
    screenshot LONGBLOB COMMENT '培训截图（二进制存储）',
    screenshot_name VARCHAR(100) COMMENT '图片文件名',
    screenshot_type VARCHAR(50) COMMENT '图片类型(image/jpeg等)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_report_id (report_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='培训记录明细表';

-- =============================================
-- 8. 大模型配置表
-- =============================================
DROP TABLE IF EXISTS t_model_config;
CREATE TABLE t_model_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(50) NOT NULL COMMENT '配置名称（别名）',
    provider VARCHAR(50) NOT NULL COMMENT '提供商类型（ollama/openai/custom）',
    base_url VARCHAR(255) NOT NULL COMMENT 'API基础地址',
    api_key VARCHAR(255) DEFAULT NULL COMMENT 'API密钥（加密存储）',
    models TEXT DEFAULT NULL COMMENT '可用模型列表（JSON）',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认模型（0-否,1-是）',
    status TINYINT DEFAULT 1 COMMENT '状态（0-禁用,1-启用）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_provider (provider),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='大模型配置表';

-- =============================================
-- 9. 智能体配置表
-- =============================================
DROP TABLE IF EXISTS t_agent_config;
CREATE TABLE t_agent_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    agent_code VARCHAR(50) NOT NULL COMMENT '智能体编码（唯一标识）',
    agent_name VARCHAR(100) NOT NULL COMMENT '智能体名称',
    description VARCHAR(500) DEFAULT NULL COMMENT '功能描述',
    model_config_id BIGINT DEFAULT NULL COMMENT '关联的大模型配置ID',
    model_name VARCHAR(100) DEFAULT NULL COMMENT '使用的具体模型名称',
    temperature DECIMAL(3,2) DEFAULT 0.70 COMMENT '温度参数（0-2）',
    top_p DECIMAL(3,2) DEFAULT 0.90 COMMENT 'Top P参数（0-1）',
    repetition_penalty DECIMAL(3,2) DEFAULT 1.10 COMMENT '重复惩罚（0-2）',
    system_prompt TEXT COMMENT '系统提示词',
    max_tokens INT DEFAULT 2048 COMMENT '最大输出token数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_agent_code (agent_code),
    INDEX idx_model_config_id (model_config_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能体配置表';

-- =============================================
-- 初始化智能体数据
-- =============================================
INSERT INTO t_agent_config (agent_code, agent_name, description, temperature, top_p, repetition_penalty, system_prompt, max_tokens) VALUES
('participant_parser', '与会人员识别', '从签到文本中识别与会人员的姓名和地市', 0.70, 0.90, 1.10, '从文本中提取与会人员的姓名和地市。\n江苏省地市：南京、苏州、无锡、常州、镇江、扬州、泰州、南通、盐城、淮安、连云港、徐州、宿迁\n输入格式通常是"地市+姓名"，如"苏州任丽华"表示地市=苏州，姓名=任丽华，任何空格或非中文都视为连接符\n每一行是一次签到，如果没有地市前缀，city字段留空\n只输出JSON数组：[{"name":"姓名","city":"地市"}]', 2048),
('training_parser', '转培记录识别', '从表格文本中提取培训记录信息', 0.50, 0.90, 1.10, '从表格文本中提取培训记录信息。\n江苏省地市：南京、苏州、无锡、常州、镇江、扬州、泰州、南通、盐城、淮安、连云港、徐州、宿迁\n每一行都是一次培训记录，不要遗漏数据，也不要过度思考\n字段映射规则（第一行为表头）：\n- 地市（可能叫"地市"或"归属"）-> city\n- 组织人员 -> organizer\n- 辅导/培训主题或主体 -> trainingContent\n- 辅导/培训时间 -> trainingTime\n- 覆盖人数 -> coverageCount\n时间格式处理：\n- 转换为当年日期，格式为yyyy-MM-dd\n只输出JSON数组：[{"city":"徐州","organizer":"组织人员","trainingContent":"培训内容","trainingTime":"2026-03-03","coverageCount":"10"}]', 2048);

-- =============================================
-- MySQL 配置建议
-- =============================================

-- 修复 MySQL max_allowed_packet 限制（解决上传大图片时报错 PacketTooBigException）
-- 执行以下SQL即可立即生效：
SET GLOBAL max_allowed_packet = 67108864;  -- 64MB

-- 查看当前设置
SHOW VARIABLES LIKE 'max_allowed_packet';

-- =============================================
-- 脚本执行完成
-- =============================================
SELECT '数据库初始化完成' AS result;
