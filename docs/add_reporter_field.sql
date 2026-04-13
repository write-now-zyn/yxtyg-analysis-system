-- =============================================
-- 转培上报记录表添加上报人字段
-- 修改日期：2026-03-31
-- 修改内容：添加reporter字段
-- =============================================

USE yxtyg_db;

-- 添加上报人字段
ALTER TABLE t_training_report 
ADD COLUMN reporter VARCHAR(50) DEFAULT NULL COMMENT '上报人' AFTER report_time;

-- 脚本执行完成
SELECT '添加上报人字段成功' AS result;
