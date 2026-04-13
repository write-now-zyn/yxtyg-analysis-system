-- =============================================
-- 模型设置模块数据库更新脚本（带字符集指定）
-- 创建时间：2026-03-31
-- 说明：在现有数据库中添加模型配置相关表
-- =============================================

-- 临时设置字符集为UTF-8
SET NAMES utf8mb4;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;

USE yxtyg_db;

-- =============================================
-- 1. 大模型配置表
-- =============================================
CREATE TABLE IF NOT EXISTS t_model_config (
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
-- 2. 智能体配置表
-- =============================================
CREATE TABLE IF NOT EXISTS t_agent_config (
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
-- 3. 清空现有数据并重新初始化智能体数据
-- =============================================
DELETE FROM t_agent_config;

INSERT INTO t_agent_config (agent_code, agent_name, description, temperature, top_p, repetition_penalty, system_prompt, max_tokens)
VALUES 
('participant_parser', '与会人员识别', '从签到文本中识别与会人员的姓名和地市', 0.70, 0.90, 1.10, '从文本中提取与会人员的姓名和地市。\n江苏省地市：南京、苏州、无锡、常州、镇江、扬州、泰州、南通、盐城、淮安、连云港、徐州、宿迁\n输入格式通常是"地市+姓名"，如"苏州任丽华"表示地市=苏州，姓名=任丽华，任何空格或非中文都视为连接符\n每一行是一次签到，如果没有地市前缀，city字段留空\n只输出JSON数组：[{"name":"姓名","city":"地市"}]', 2048),
('training_parser', '转培记录识别', '从表格文本中提取培训记录信息', 0.50, 0.90, 1.10, '从表格文本中提取培训记录信息。\n江苏省地市：南京、苏州、无锡、常州、镇江、扬州、泰州、南通、盐城、淮安、连云港、徐州、宿迁\n每一行都是一次培训记录，不要遗漏数据，也不要过度思考\n字段映射规则（第一行为表头）：\n- 地市（可能叫"地市"或"归属"）-> city\n- 组织人员 -> organizer\n- 辅导/培训主题或主体 -> trainingContent\n- 辅导/培训时间 -> trainingTime\n- 覆盖人数 -> coverageCount\n时间格式处理：\n- 转换为当年日期，格式为yyyy-MM-dd\n只输出JSON数组：[{"city":"徐州","organizer":"组织人员","trainingContent":"培训内容","trainingTime":"2026-03-03","coverageCount":"10"}]', 2048);

-- =============================================
-- 脚本执行完成
-- =============================================
SELECT '模型设置模块数据库更新完成' AS result;
