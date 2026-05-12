package com.jscm.yxtyg.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jscm.yxtyg.entity.ModelConfig;
import com.jscm.yxtyg.service.LlmClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 大模型客户端服务实现
 */
@Slf4j
@Service
public class LlmClientServiceImpl implements LlmClientService {

    private static final String PROVIDER_OLLAMA = "ollama";
    private static final String PROVIDER_ANTHROPIC = "anthropic";
    private static final String ANTHROPIC_VERSION = "2023-06-01";
    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 10000;
    private static final int DEFAULT_READ_TIMEOUT_MS = 30000;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean testConnection(ModelConfig config) {
        try {
            String url = buildModelsUrl(config);
            ResponseEntity<String> response = getRestTemplate(DEFAULT_CONNECT_TIMEOUT_MS, DEFAULT_READ_TIMEOUT_MS)
                    .exchange(url, HttpMethod.GET, new HttpEntity<>(buildHeaders(config)), String.class);
            boolean success = response.getStatusCode().is2xxSuccessful();
            log.info("测试模型连接结果：provider={}, url={}, success={}", config.getProvider(), url, success);
            return success;
        } catch (Exception e) {
            log.error("测试模型连接失败，provider={}, baseUrl={}", config.getProvider(), config.getBaseUrl(), e);
            return false;
        }
    }

    @Override
    public List<String> fetchModels(ModelConfig config) throws Exception {
        String url = buildModelsUrl(config);
        ResponseEntity<String> response = getRestTemplate(DEFAULT_CONNECT_TIMEOUT_MS, DEFAULT_READ_TIMEOUT_MS)
                .exchange(url, HttpMethod.GET, new HttpEntity<>(buildHeaders(config)), String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            return new ArrayList<>();
        }

        JsonNode root = objectMapper.readTree(response.getBody());
        List<String> models = new ArrayList<>();

        if (isOllama(config)) {
            JsonNode modelsNode = root.path("models");
            if (modelsNode.isArray()) {
                for (JsonNode modelNode : modelsNode) {
                    String modelName = modelNode.path("name").asText();
                    if (StringUtils.hasText(modelName)) {
                        models.add(modelName);
                    }
                }
            }
            return models;
        }

        JsonNode dataNode = root.path("data");
        if (dataNode.isArray()) {
            for (JsonNode modelNode : dataNode) {
                String modelName = modelNode.path("id").asText();
                if (StringUtils.hasText(modelName)) {
                    models.add(modelName);
                }
            }
        }
        return models;
    }

    @Override
    public String chat(ModelConfig config, String modelName, List<Map<String, String>> messages,
                       BigDecimal temperature, BigDecimal topP, BigDecimal repetitionPenalty,
                       Integer maxTokens, Integer timeoutMs) throws Exception {
        String url = buildChatUrl(config);
        Map<String, Object> requestBody = buildChatRequestBody(config, modelName, messages, temperature, topP,
                repetitionPenalty, maxTokens);

        HttpHeaders headers = buildHeaders(config);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
        ResponseEntity<String> response = getRestTemplate(DEFAULT_CONNECT_TIMEOUT_MS, resolveReadTimeout(timeoutMs))
                .postForEntity(url, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("模型调用失败，HTTP状态：" + response.getStatusCode());
        }

        return extractContent(config, response.getBody());
    }

    private String extractContent(ModelConfig config, String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        String content;
        if (isOllama(config)) {
            content = root.path("message").path("content").asText();
        } else if (isAnthropic(config)) {
            content = extractAnthropicText(root.path("content"));
        } else {
            content = root.path("choices").path(0).path("message").path("content").asText();
        }

        if (!StringUtils.hasText(content)) {
            throw new IllegalStateException("模型未返回有效内容");
        }
        return content;
    }

    private Map<String, Object> buildChatRequestBody(ModelConfig config, String modelName,
                                                     List<Map<String, String>> messages,
                                                     BigDecimal temperature, BigDecimal topP,
                                                     BigDecimal repetitionPenalty, Integer maxTokens) {
        if (isAnthropic(config)) {
            return buildAnthropicRequestBody(modelName, messages, temperature, topP, maxTokens);
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelName);
        requestBody.put("messages", messages);

        if (isOllama(config)) {
            requestBody.put("stream", false);
            if (temperature != null) {
                requestBody.put("temperature", temperature.doubleValue());
            }
            if (topP != null) {
                requestBody.put("top_p", topP.doubleValue());
            }
            if (repetitionPenalty != null) {
                requestBody.put("repeat_penalty", repetitionPenalty.doubleValue());
            }
            if (maxTokens != null) {
                requestBody.put("num_predict", maxTokens);
            }
            return requestBody;
        }

        if (temperature != null) {
            requestBody.put("temperature", temperature.doubleValue());
        }
        if (topP != null) {
            requestBody.put("top_p", topP.doubleValue());
        }
        if (repetitionPenalty != null) {
            requestBody.put("repetition_penalty", repetitionPenalty.doubleValue());
        }
        if (maxTokens != null) {
            requestBody.put("max_tokens", maxTokens);
        }
        return requestBody;
    }

    private Map<String, Object> buildAnthropicRequestBody(String modelName, List<Map<String, String>> messages,
                                                          BigDecimal temperature, BigDecimal topP,
                                                          Integer maxTokens) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelName);
        requestBody.put("max_tokens", maxTokens == null || maxTokens <= 0 ? 2048 : maxTokens);

        if (temperature != null) {
            requestBody.put("temperature", temperature.doubleValue());
        }
        if (topP != null) {
            requestBody.put("top_p", topP.doubleValue());
        }

        List<Map<String, String>> anthropicMessages = new ArrayList<>();
        String systemPrompt = null;
        for (Map<String, String> message : messages) {
            String role = message.get("role");
            String content = message.get("content");
            if ("system".equals(role)) {
                if (!StringUtils.hasText(systemPrompt)) {
                    systemPrompt = content;
                }
                continue;
            }
            anthropicMessages.add(message);
        }

        if (StringUtils.hasText(systemPrompt)) {
            requestBody.put("system", systemPrompt);
        }
        requestBody.put("messages", anthropicMessages);
        return requestBody;
    }

    private HttpHeaders buildHeaders(ModelConfig config) {
        HttpHeaders headers = new HttpHeaders();
        if (isAnthropic(config) && StringUtils.hasText(config.getApiKey())) {
            headers.set("x-api-key", config.getApiKey());
            headers.set("anthropic-version", ANTHROPIC_VERSION);
            return headers;
        }
        if (StringUtils.hasText(config.getApiKey())) {
            headers.setBearerAuth(config.getApiKey());
        }
        return headers;
    }

    private String buildModelsUrl(ModelConfig config) {
        String baseUrl = normalizeBaseUrl(config.getBaseUrl());
        if (isOllama(config)) {
            return baseUrl + "/api/tags";
        }
        if (isAnthropic(config)) {
            return baseUrl + "/v1/models";
        }
        return baseUrl + "/models";
    }

    private String buildChatUrl(ModelConfig config) {
        String baseUrl = normalizeBaseUrl(config.getBaseUrl());
        if (isOllama(config)) {
            return baseUrl + "/api/chat";
        }
        if (isAnthropic(config)) {
            return baseUrl + "/v1/messages";
        }
        return baseUrl + "/chat/completions";
    }

    private boolean isOllama(ModelConfig config) {
        return config != null && PROVIDER_OLLAMA.equalsIgnoreCase(config.getProvider());
    }

    private boolean isAnthropic(ModelConfig config) {
        return config != null && PROVIDER_ANTHROPIC.equalsIgnoreCase(config.getProvider());
    }

    private String extractAnthropicText(JsonNode contentNode) {
        if (!contentNode.isArray()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (JsonNode block : contentNode) {
            if ("text".equals(block.path("type").asText())) {
                builder.append(block.path("text").asText());
            }
        }
        return builder.toString();
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (!StringUtils.hasText(baseUrl)) {
            throw new IllegalArgumentException("baseUrl不能为空");
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private int resolveReadTimeout(Integer timeoutMs) {
        return timeoutMs == null || timeoutMs <= 0 ? 300000 : timeoutMs;
    }

    private RestTemplate getRestTemplate(int connectTimeoutMs, int readTimeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        return new RestTemplate(factory);
    }
}
