package com.jscm.yxtyg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jscm.yxtyg.entity.AgentConfig;
import com.jscm.yxtyg.entity.ModelConfig;
import com.jscm.yxtyg.exception.BusinessException;
import com.jscm.yxtyg.mapper.AgentConfigMapper;
import com.jscm.yxtyg.mapper.ModelConfigMapper;
import com.jscm.yxtyg.service.ModelConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 大模型配置服务实现类
 */
@Slf4j
@Service
public class ModelConfigServiceImpl extends ServiceImpl<ModelConfigMapper, ModelConfig> implements ModelConfigService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate;
    
    @Autowired
    private AgentConfigMapper agentConfigMapper;

    public ModelConfigServiceImpl() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(30000);
        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    public List<ModelConfig> listAll() {
        log.debug("获取所有大模型配置");
        LambdaQueryWrapper<ModelConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ModelConfig::getIsDefault)
               .orderByDesc(ModelConfig::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveConfig(ModelConfig config) {
        log.info("保存大模型配置，ID：{}，名称：{}", config.getId(), config.getName());

        ModelConfig existingConfig = null;
        if (config.getId() != null) {
            existingConfig = this.getById(config.getId());
            if (existingConfig == null) {
                throw new BusinessException(400, "配置不存在");
            }
        }

        normalizeDefaultState(config, existingConfig);

        if (config.getId() == null) {
            config.setCreateTime(LocalDateTime.now());
            config.setUpdateTime(LocalDateTime.now());
            this.save(config);
        } else {
            config.setUpdateTime(LocalDateTime.now());
            this.updateById(config);
        }
        
        log.info("保存大模型配置成功，ID：{}", config.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfig(Long id) {
        log.info("删除大模型配置，ID：{}", id);

        ModelConfig config = this.getById(id);
        if (config == null) {
            throw new BusinessException(400, "配置不存在");
        }

        LambdaQueryWrapper<AgentConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentConfig::getModelConfigId, id);
        List<AgentConfig> agents = agentConfigMapper.selectList(wrapper);

        if (!agents.isEmpty()) {
            String agentNames = agents.stream()
                    .map(AgentConfig::getAgentName)
                    .collect(Collectors.joining("、"));
            throw new BusinessException(400, "该模型配置正在被以下智能体使用：" + agentNames + "，无法删除");
        }

        this.removeById(id);

        if (Integer.valueOf(1).equals(config.getIsDefault())) {
            ensureEnabledDefaultConfig();
        }

        log.info("删除大模型配置成功");
    }

    @Override
    public ModelConfig getDefaultConfig() {
        log.debug("获取默认大模型配置");
        LambdaQueryWrapper<ModelConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelConfig::getIsDefault, 1)
               .eq(ModelConfig::getStatus, 1)
               .last("LIMIT 1");
        return this.getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultConfig(Long id) {
        log.info("设置默认大模型配置，ID：{}", id);

        ModelConfig config = this.getById(id);
        if (config == null) {
            throw new BusinessException(400, "配置不存在");
        }

        if (!Integer.valueOf(1).equals(config.getStatus())) {
            throw new BusinessException(400, "启用中的配置才能设为默认");
        }

        clearOtherDefaults(id);
        config.setIsDefault(1);
        config.setUpdateTime(LocalDateTime.now());
        this.updateById(config);

        log.info("设置默认大模型配置成功");
    }

    @Override
    public boolean testConnection(Long id) {
        log.info("测试大模型连接，ID：{}", id);
        
        ModelConfig config = this.getById(id);
        if (config == null) {
            throw new BusinessException(400, "配置不存在");
        }
        
        return testConnection(config);
    }

    @Override
    public boolean testConnection(ModelConfig config) {
        log.info("测试大模型连接，配置：{}", config.getName());
        
        try {
            String testUrl = config.getBaseUrl();
            if ("ollama".equalsIgnoreCase(config.getProvider())) {
                testUrl = config.getBaseUrl() + "/api/tags";
            }
            
            HttpHeaders headers = new HttpHeaders();
            if (config.getApiKey() != null && !config.getApiKey().isEmpty()) {
                headers.set("Authorization", "Bearer " + config.getApiKey());
            }
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                testUrl, 
                HttpMethod.GET, 
                entity, 
                String.class
            );
            
            boolean success = response.getStatusCode() == HttpStatus.OK;
            log.info("测试连接结果：{}", success ? "成功" : "失败");
            return success;
        } catch (Exception e) {
            log.error("测试连接失败", e);
            return false;
        }
    }

    @Override
    public List<String> fetchModels(Long id) {
        log.info("拉取模型列表，ID：{}", id);

        ModelConfig config = this.getById(id);
        if (config == null) {
            throw new BusinessException(400, "配置不存在");
        }

        List<String> models = new ArrayList<>();

        try {
            if ("ollama".equalsIgnoreCase(config.getProvider())) {
                String url = config.getBaseUrl() + "/api/tags";

                HttpHeaders headers = new HttpHeaders();
                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
                );

                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    JsonNode root = objectMapper.readTree(response.getBody());
                    JsonNode modelsNode = root.path("models");
                    if (modelsNode.isArray()) {
                        for (JsonNode modelNode : modelsNode) {
                            String modelName = modelNode.path("name").asText();
                            if (!modelName.isEmpty()) {
                                models.add(modelName);
                            }
                        }
                    }
                }
            }
            
            if (!models.isEmpty()) {
                config.setModels(objectMapper.writeValueAsString(models));
                config.setUpdateTime(LocalDateTime.now());
                this.updateById(config);
            }

            log.info("拉取模型列表成功，共{}个模型", models.size());
        } catch (Exception e) {
            log.error("拉取模型列表失败", e);
            throw new BusinessException(500, "拉取模型列表失败：" + e.getMessage());
        }

        return models;
    }

    private void normalizeDefaultState(ModelConfig config, ModelConfig existingConfig) {
        boolean enabled = !Integer.valueOf(0).equals(config.getStatus());
        boolean wantsDefault = Integer.valueOf(1).equals(config.getIsDefault());
        boolean currentIsDefault = existingConfig != null && Integer.valueOf(1).equals(existingConfig.getIsDefault());
        long otherEnabledDefaultCount = countEnabledDefaults(config.getId());

        if (wantsDefault && !enabled) {
            throw new BusinessException(400, "默认配置必须为启用状态");
        }

        if (wantsDefault) {
            clearOtherDefaults(config.getId());
            return;
        }

        if (currentIsDefault && otherEnabledDefaultCount == 0) {
            throw new BusinessException(400, "请先设置其他启用中的默认配置");
        }

        if (enabled && otherEnabledDefaultCount == 0) {
            config.setIsDefault(1);
            clearOtherDefaults(config.getId());
        }
    }

    private long countEnabledDefaults(Long excludeId) {
        LambdaQueryWrapper<ModelConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelConfig::getIsDefault, 1)
                .eq(ModelConfig::getStatus, 1);
        if (excludeId != null) {
            wrapper.ne(ModelConfig::getId, excludeId);
        }
        return this.count(wrapper);
    }

    private void clearOtherDefaults(Long keepId) {
        LambdaUpdateWrapper<ModelConfig> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(ModelConfig::getIsDefault, 0)
                .eq(ModelConfig::getIsDefault, 1);
        if (keepId != null) {
            updateWrapper.ne(ModelConfig::getId, keepId);
        }
        this.update(updateWrapper);
    }

    private void ensureEnabledDefaultConfig() {
        if (getDefaultConfig() != null) {
            return;
        }

        LambdaQueryWrapper<ModelConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelConfig::getStatus, 1)
                .orderByDesc(ModelConfig::getCreateTime)
                .last("LIMIT 1");
        ModelConfig fallback = this.getOne(wrapper);
        if (fallback == null) {
            return;
        }

        clearOtherDefaults(fallback.getId());
        fallback.setIsDefault(1);
        fallback.setUpdateTime(LocalDateTime.now());
        this.updateById(fallback);
    }
}
