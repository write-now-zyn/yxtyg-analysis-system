-- =============================================
-- 智能体配置表更新脚本 - 添加用户提示词字段
-- 创建时间：2026-03-31
-- =============================================

SET NAMES utf8mb4;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;

USE yxtyg_db;

-- 添加用户提示词字段
ALTER TABLE t_agent_config ADD COLUMN user_prompt TEXT COMMENT '用户提示词模板（支持{{content}}占位符）' AFTER system_prompt;

-- 更新现有智能体的用户提示词
UPDATE t_agent_config SET user_prompt = '{{content}}' WHERE agent_code = 'participant_parser';
UPDATE t_agent_config SET user_prompt = '{{content}}' WHERE agent_code = 'training_parser';

SELECT '智能体配置表更新完成' AS result;
