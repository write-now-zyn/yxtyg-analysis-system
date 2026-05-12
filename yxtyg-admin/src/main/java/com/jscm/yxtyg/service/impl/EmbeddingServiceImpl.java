package com.jscm.yxtyg.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jscm.yxtyg.entity.EmbeddingConfig;
import com.jscm.yxtyg.exception.BusinessException;
import com.jscm.yxtyg.service.EmbeddingConfigService;
import com.jscm.yxtyg.service.EmbeddingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Embedding 服务实现
 */
@Slf4j
@Service
public class EmbeddingServiceImpl implements EmbeddingService {

    @Autowired
    private EmbeddingConfigService embeddingConfigService;

    @Autowired
    private ObjectMapper objectMapper;

    private volatile RestTemplate restTemplate;

    @Override
    public List<Double> embed(String content) {
        if (!StringUtils.hasText(content)) {
            throw new BusinessException(400, "工单内容不能为空");
        }
        List<List<Double>> embeddings = embedBatch(Collections.singletonList(content));
        return embeddings.isEmpty() ? Collections.emptyList() : embeddings.get(0);
    }

    @Override
    public List<List<Double>> embedBatch(List<String> contents) {
        if (CollectionUtils.isEmpty(contents)) {
            return Collections.emptyList();
        }
        EmbeddingConfig embeddingConfig = embeddingConfigService.getDefaultConfig();
        if (embeddingConfig == null
                || !StringUtils.hasText(embeddingConfig.getBaseUrl())
                || !StringUtils.hasText(embeddingConfig.getModel())) {
            throw new BusinessException(400, "请先配置向量化模型");
        }

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", embeddingConfig.getModel());
            body.put("input", contents.size() == 1 ? contents.get(0) : contents);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (StringUtils.hasText(embeddingConfig.getApiKey())) {
                headers.setBearerAuth(embeddingConfig.getApiKey());
            }

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
            ResponseEntity<String> response = getRestTemplate().postForEntity(
                    embeddingConfig.getBaseUrl(),
                    entity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new BusinessException(500, "Embedding 接口调用失败");
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode embeddingsNode = root.path("embeddings");
            if (!embeddingsNode.isArray()) {
                throw new BusinessException(500, "Embedding 接口返回格式不正确");
            }

            List<List<Double>> result = new ArrayList<>();
            for (JsonNode embeddingNode : embeddingsNode) {
                List<Double> vector = new ArrayList<>();
                if (embeddingNode.isArray()) {
                    for (JsonNode dimension : embeddingNode) {
                        vector.add(dimension.asDouble());
                    }
                }
                result.add(vector);
            }
            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用 Embedding 接口失败", e);
            throw new BusinessException(500, "调用 Embedding 接口失败：" + e.getMessage());
        }
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
