package com.jscm.yxtyg.service;

import com.jscm.yxtyg.entity.ModelConfig;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 大模型客户端服务
 */
public interface LlmClientService {

    boolean testConnection(ModelConfig config);

    List<String> fetchModels(ModelConfig config) throws Exception;

    String chat(ModelConfig config, String modelName, List<Map<String, String>> messages,
                BigDecimal temperature, BigDecimal topP, BigDecimal repetitionPenalty,
                Integer maxTokens, Integer timeoutMs) throws Exception;
}
