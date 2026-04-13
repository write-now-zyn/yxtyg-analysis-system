-- =============================================
-- 一线体验官专项数据分析系统 - 数据库初始化脚本
-- 数据库：MySQL 8.0
-- 创建时间：2026-03-30
-- 版本：1.0.0
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
    report_month VARCHAR(10) NOT NULL COMMENT '上报月份（YYYY-MM）',
    record_count INT DEFAULT 0 COMMENT '培训记录条数',
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
-- 初始化数据
-- =============================================

-- 插入江苏移动地市列表（基础数据）
INSERT INTO t_experimenter (city, name, phone, is_contact) VALUES
('南京', '张三', '13800138001', 1),
('苏州', '李四', '13800138002', 1),
('无锡', '王五', '13800138003', 1),
('常州', '赵六', '13800138004', 1),
('徐州', '钱七', '13800138005', 1),
('南通', '孙八', '13800138006', 1),
('连云港', '周九', '13800138007', 1),
('淮安', '吴十', '13800138008', 1),
('盐城', '郑一', '13800138009', 1),
('扬州', '王二', '13800138010', 1),
('镇江', '陈三', '13800138011', 1),
('泰州', '林四', '13800138012', 1),
('宿迁', '黄五', '13800138013', 1);

-- 插入示例评审会议数据
INSERT INTO t_review_meeting (demand_no, title, meeting_time, participants, content) VALUES
('REQ-2026-001', '5G网络优化方案评审', '2026-03-15 14:00:00', '[{"name":"张三","city":"南京"},{"name":"李四","city":"苏州"}]', '讨论了5G网络覆盖优化方案，确定了实施计划和时间节点。'),
('REQ-2026-002', '客户满意度提升方案评审', '2026-03-20 10:00:00', '[{"name":"王五","city":"无锡"},{"name":"赵六","city":"常州"}]', '分析了客户满意度现状，制定了提升措施和考核标准。');

-- 插入示例转培上报记录
INSERT INTO t_training_report (city, report_month, record_count) VALUES
('南京', '2026-03', 3),
('苏州', '2026-03', 2),
('无锡', '2026-03', 1);

-- 插入示例培训记录
INSERT INTO t_training_record (report_id, training_time, organizer, training_subject, coverage_count, training_content) VALUES
(1, '2026-03-10 09:00:00', '张三', '5G网络基础知识', 20, '5G网络架构、技术特点和应用场景培训'),
(1, '2026-03-15 14:00:00', '张三', '5G业务操作流程', 15, '5G相关业务的办理流程和操作规范培训'),
(1, '2026-03-20 10:00:00', '张三', '5G客户服务技巧', 18, '5G客户咨询处理和服务技巧培训'),
(2, '2026-03-12 15:00:00', '李四', '4G网络优化', 12, '4G网络优化技术和实践培训'),
(2, '2026-03-18 09:00:00', '李四', '网络故障排查', 10, '常见网络故障的排查方法和解决方案培训'),
(3, '2026-03-16 14:00:00', '王五', '宽带安装与维护', 8, '宽带安装技术和日常维护培训');

-- =============================================
-- 脚本执行完成
-- =============================================
SELECT '数据库初始化完成' AS result;
