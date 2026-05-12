CREATE TABLE IF NOT EXISTS t_embedding_config (
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
