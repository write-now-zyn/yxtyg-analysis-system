package com.jscm.yxtyg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jscm.yxtyg.entity.EmbeddingConfig;

import java.util.List;

/**
 * 向量化模型配置服务接口
 */
public interface EmbeddingConfigService extends IService<EmbeddingConfig> {

    List<EmbeddingConfig> listAll();

    void saveConfig(EmbeddingConfig config);

    void deleteConfig(Long id);

    EmbeddingConfig getDefaultConfig();

    void setDefaultConfig(Long id);

    boolean testConnection(Long id);

    boolean testConnection(EmbeddingConfig config);
}
