-- =============================================
-- 修复 MySQL max_allowed_packet 限制
-- 修改日期：2026-03-19
-- 问题描述：上传大图片时报错 PacketTooBigException
-- 解决方案：增大 max_allowed_packet 到 64MB
-- =============================================

-- 方式一：临时修改（重启后失效，不需要重启MySQL）
-- 执行以下SQL即可立即生效：
SET GLOBAL max_allowed_packet = 67108864;  -- 64MB

-- 查看当前设置
SHOW VARIABLES LIKE 'max_allowed_packet';

-- =============================================
-- 方式二：永久修改（需要重启MySQL）
-- 在 MySQL 配置文件 my.ini (Windows) 或 my.cnf (Linux) 中添加：
-- 
-- [mysqld]
-- max_allowed_packet = 64M
-- 
-- 然后重启 MySQL 服务
-- =============================================
