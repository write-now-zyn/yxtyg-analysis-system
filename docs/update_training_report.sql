-- =============================================
-- 转培上报记录表结构修改
-- 修改日期：2026-03-18
-- 修改内容：
--   1. 增加上报时间字段 report_time
--   2. 增加培训人数字段 training_person_count
--   3. 修改 report_month 注释为"培训月份"
-- =============================================

USE yxtyg_db;

-- 添加上报时间字段
ALTER TABLE t_training_report 
ADD COLUMN report_time DATE DEFAULT NULL COMMENT '上报时间' AFTER report_month;

-- 添加培训人数字段
ALTER TABLE t_training_report 
ADD COLUMN training_person_count INT DEFAULT 0 COMMENT '培训人数（覆盖人数合计）' AFTER record_count;

-- 修改 report_month 字段注释
ALTER TABLE t_training_report 
MODIFY COLUMN report_month VARCHAR(10) NOT NULL COMMENT '培训月份 YYYY-MM';

-- 更新培训人数（根据已有数据计算）
UPDATE t_training_report tr
SET training_person_count = (
    SELECT COALESCE(SUM(coverage_count), 0)
    FROM t_training_record trec
    WHERE trec.report_id = tr.id
);
