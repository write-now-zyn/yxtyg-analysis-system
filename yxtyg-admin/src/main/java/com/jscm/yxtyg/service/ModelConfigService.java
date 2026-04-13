package com.jscm.yxtyg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jscm.yxtyg.entity.ModelConfig;

import java.util.List;

/**
 * 大模型配置服务接口
 */
public interface ModelConfigService extends IService<ModelConfig> {

    List<ModelConfig> listAll();

    void saveConfig(ModelConfig config);

    void deleteConfig(Long id);

    ModelConfig getDefaultConfig();

    void setDefaultConfig(Long id);

    boolean testConnection(Long id);

    boolean testConnection(ModelConfig config);

    List<String> fetchModels(Long id);
}
