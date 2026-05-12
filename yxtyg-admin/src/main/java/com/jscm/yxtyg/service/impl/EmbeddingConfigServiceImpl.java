package com.jscm.yxtyg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jscm.yxtyg.entity.EmbeddingConfig;
import com.jscm.yxtyg.exception.BusinessException;
import com.jscm.yxtyg.mapper.EmbeddingConfigMapper;
import com.jscm.yxtyg.service.EmbeddingConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 向量化模型配置服务实现类
 */
@Slf4j
@Service
public class EmbeddingConfigServiceImpl extends ServiceImpl<EmbeddingConfigMapper, EmbeddingConfig> implements EmbeddingConfigService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private volatile RestTemplate restTemplate;

    @Override
    public List<EmbeddingConfig> listAll() {
        LambdaQueryWrapper<EmbeddingConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(EmbeddingConfig::getIsDefault)
                .orderByDesc(EmbeddingConfig::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveConfig(EmbeddingConfig config) {
        validateConfig(config);

        EmbeddingConfig existingConfig = null;
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
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfig(Long id) {
        EmbeddingConfig config = this.getById(id);
        if (config == null) {
            throw new BusinessException(400, "配置不存在");
        }

        this.removeById(id);

        if (Integer.valueOf(1).equals(config.getIsDefault())) {
            ensureEnabledDefaultConfig();
        }
    }

    @Override
    public EmbeddingConfig getDefaultConfig() {
        LambdaQueryWrapper<EmbeddingConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmbeddingConfig::getIsDefault, 1)
                .eq(EmbeddingConfig::getStatus, 1)
                .last("LIMIT 1");
        return this.getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultConfig(Long id) {
        EmbeddingConfig config = this.getById(id);
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
    }

    @Override
    public boolean testConnection(Long id) {
        EmbeddingConfig config = this.getById(id);
        if (config == null) {
            throw new BusinessException(400, "配置不存在");
        }
        return testConnection(config);
    }

    @Override
    public boolean testConnection(EmbeddingConfig config) {
        validateConfig(config);
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", config.getModel());
            body.put("input", "test");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (StringUtils.hasText(config.getApiKey())) {
                headers.setBearerAuth(config.getApiKey());
            }

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
            ResponseEntity<String> response = getRestTemplate().postForEntity(config.getBaseUrl(), entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return false;
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("embeddings").isArray();
        } catch (Exception e) {
            log.warn("测试向量化模型连接失败：{}", e.getMessage());
            return false;
        }
    }

    private void validateConfig(EmbeddingConfig config) {
        if (config == null) {
            throw new BusinessException(400, "配置不能为空");
        }
        if (!StringUtils.hasText(config.getName())) {
            throw new BusinessException(400, "请输入配置名称");
        }
        if (!StringUtils.hasText(config.getProvider())) {
            throw new BusinessException(400, "请选择提供商");
        }
        if (!StringUtils.hasText(config.getBaseUrl())) {
            throw new BusinessException(400, "请输入API地址");
        }
        if (!StringUtils.hasText(config.getModel())) {
            throw new BusinessException(400, "请输入模型名称");
        }
        if (config.getStatus() == null) {
            config.setStatus(1);
        }
        if (config.getIsDefault() == null) {
            config.setIsDefault(0);
        }
    }

    private void normalizeDefaultState(EmbeddingConfig config, EmbeddingConfig existingConfig) {
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
        LambdaQueryWrapper<EmbeddingConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmbeddingConfig::getIsDefault, 1)
                .eq(EmbeddingConfig::getStatus, 1);
        if (excludeId != null) {
            wrapper.ne(EmbeddingConfig::getId, excludeId);
        }
        return this.count(wrapper);
    }

    private void clearOtherDefaults(Long keepId) {
        LambdaUpdateWrapper<EmbeddingConfig> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(EmbeddingConfig::getIsDefault, 0)
                .eq(EmbeddingConfig::getIsDefault, 1);
        if (keepId != null) {
            updateWrapper.ne(EmbeddingConfig::getId, keepId);
        }
        this.update(updateWrapper);
    }

    private void ensureEnabledDefaultConfig() {
        if (getDefaultConfig() != null) {
            return;
        }

        LambdaQueryWrapper<EmbeddingConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmbeddingConfig::getStatus, 1)
                .orderByDesc(EmbeddingConfig::getCreateTime)
                .last("LIMIT 1");
        EmbeddingConfig fallback = this.getOne(wrapper);
        if (fallback == null) {
            return;
        }

        clearOtherDefaults(fallback.getId());
        fallback.setIsDefault(1);
        fallback.setUpdateTime(LocalDateTime.now());
        this.updateById(fallback);
    }

    private RestTemplate getRestTemplate() {
        if (restTemplate == null) {
            synchronized (this) {
                if (restTemplate == null) {
                    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                    factory.setConnectTimeout(30000);
                    factory.setReadTimeout(300000);
                    restTemplate = new RestTemplate(factory);
                }
            }
        }
        return restTemplate;
    }
}
