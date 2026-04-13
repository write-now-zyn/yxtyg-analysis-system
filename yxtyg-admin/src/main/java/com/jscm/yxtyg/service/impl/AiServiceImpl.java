package com.jscm.yxtyg.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jscm.yxtyg.entity.AgentConfig;
import com.jscm.yxtyg.entity.ModelConfig;
import com.jscm.yxtyg.service.AgentConfigService;
import com.jscm.yxtyg.service.ModelConfigService;
import com.jscm.yxtyg.service.AiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

/**
 * AI服务实现类 - 支持动态配置大模型和智能体参数
 */
@Slf4j
@Service
public class AiServiceImpl implements AiService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private RestTemplate restTemplate;

    @Autowired
    private ModelConfigService modelConfigService;

    @Autowired
    private AgentConfigService agentConfigService;

    private RestTemplate getRestTemplate() {
        if (restTemplate == null) {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(300000);
            factory.setReadTimeout(300000);
            restTemplate = new RestTemplate(factory);
        }
        return restTemplate;
    }

    private static final String DEFAULT_SYSTEM_PROMPT = 
        "从文本中提取与会人员的姓名和地市。\n" +
        "江苏省地市：南京、苏州、无锡、常州、镇江、扬州、泰州、南通、盐城、淮安、连云港、徐州、宿迁\n" +
        "输入格式通常是\"地市+姓名\"，如\"苏州任丽华\"表示地市=苏州，姓名=任丽华，任何空格或非中文都视为连接符\n" +
        "每一行是一次签到，如果没有地市前缀，city字段留空\n" +
        "只输出JSON数组：[{\"name\":\"姓名\",\"city\":\"地市\"}]\n";

    private static final String DEFAULT_TRAINING_PROMPT = 
        "从表格文本中提取培训记录信息。\n" +
        "江苏省地市：南京、苏州、无锡、常州、镇江、扬州、泰州、南通、盐城、淮安、连云港、徐州、宿迁\n" +
        "每一行都是一次培训记录，不要遗漏数据，也不要过度思考\n" +
        "字段映射规则（第一行为表头）：\n" +
        "- 地市（可能叫\"地市\"或\"归属\"）-> city\n" +
        "- 组织人员 -> organizer\n" +
        "- 辅导/培训主题或主体 -> trainingContent\n" +
        "- 辅导/培训时间 -> trainingTime\n" +
        "- 覆盖人数 -> coverageCount\n" +
        "时间格式处理：\n" +
        "- 转换为当年日期，格式为yyyy-MM-dd\n" +
        "只输出JSON数组：[{\"city\":\"徐州\",\"organizer\":\"组织人员\",\"trainingContent\":\"培训内容\",\"trainingTime\":\"2026-03-03\",\"coverageCount\":\"10\"}]\n";

    @Override
    public List<Map<String, String>> parseParticipants(String text) {
        log.info("开始解析与会人员文本：{}", text);
        
        try {
            AgentConfig agentConfig = agentConfigService.getByCode("participant_parser");
            if (agentConfig == null) {
                log.warn("未找到与会人员识别智能体配置，使用默认配置");
                return parseWithDefaultConfig(text, DEFAULT_SYSTEM_PROMPT);
            }
            
            return parseWithAgentConfig(text, agentConfig);
        } catch (Exception e) {
            log.error("解析与会人员失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Map<String, String>> parseTrainingRecords(String text) {
        log.info("开始解析培训记录文本：{}", text);
        
        try {
            AgentConfig agentConfig = agentConfigService.getByCode("training_parser");
            if (agentConfig == null) {
                log.warn("未找到转培记录识别智能体配置，使用默认配置");
                return parseWithDefaultConfig(text, DEFAULT_TRAINING_PROMPT);
            }
            
            return parseWithAgentConfig(text, agentConfig);
        } catch (Exception e) {
            log.error("解析培训记录失败", e);
            return Collections.emptyList();
        }
    }

    private List<Map<String, String>> parseWithDefaultConfig(String text, String systemPrompt) {
        try {
            ModelConfig defaultConfig = modelConfigService.getDefaultConfig();
            if (defaultConfig == null) {
                log.error("未找到默认大模型配置");
                return Collections.emptyList();
            }
            
            String modelName = "qwen3:0.6b";
            if (defaultConfig.getModels() != null && !defaultConfig.getModels().isEmpty()) {
                try {
                    List<String> models = objectMapper.readValue(defaultConfig.getModels(), new TypeReference<List<String>>() {});
                    if (!models.isEmpty()) {
                        modelName = models.get(0);
                    }
                } catch (Exception e) {
                    log.warn("解析模型列表失败", e);
                }
            }
            
            return callModel(defaultConfig.getBaseUrl(), defaultConfig.getApiKey(), modelName, 
                           systemPrompt, "{content}", text, new BigDecimal("0.7"), new BigDecimal("0.9"), 
                           new BigDecimal("1.1"), 2048);
        } catch (Exception e) {
            log.error("使用默认配置解析失败", e);
            return Collections.emptyList();
        }
    }

    private List<Map<String, String>> parseWithAgentConfig(String text, AgentConfig agentConfig) {
        try {
            ModelConfig modelConfig = null;
            if (agentConfig.getModelConfigId() != null) {
                modelConfig = modelConfigService.getById(agentConfig.getModelConfigId());
            }
            
            if (modelConfig == null) {
                modelConfig = modelConfigService.getDefaultConfig();
            }
            
            if (modelConfig == null) {
                log.error("未找到可用的大模型配置");
                return Collections.emptyList();
            }
            
            String modelName = agentConfig.getModelName();
            if (modelName == null || modelName.isEmpty()) {
                if (modelConfig.getModels() != null && !modelConfig.getModels().isEmpty()) {
                    try {
                        List<String> models = objectMapper.readValue(modelConfig.getModels(), new TypeReference<List<String>>() {});
                        if (!models.isEmpty()) {
                            modelName = models.get(0);
                        }
                    } catch (Exception e) {
                        log.warn("解析模型列表失败", e);
                    }
                }
            }
            
            String systemPrompt = agentConfig.getSystemPrompt();
            if (systemPrompt == null || systemPrompt.isEmpty()) {
                systemPrompt = DEFAULT_SYSTEM_PROMPT;
            }
            
            String userPrompt = agentConfig.getUserPrompt();
            if (userPrompt == null || userPrompt.isEmpty()) {
                userPrompt = "{content}";
            }
            
            BigDecimal temperature = agentConfig.getTemperature();
            if (temperature == null) temperature = new BigDecimal("0.7");
            
            BigDecimal topP = agentConfig.getTopP();
            if (topP == null) topP = new BigDecimal("0.9");
            
            BigDecimal repetitionPenalty = agentConfig.getRepetitionPenalty();
            if (repetitionPenalty == null) repetitionPenalty = new BigDecimal("1.1");
            
            Integer maxTokens = agentConfig.getMaxTokens();
            if (maxTokens == null) maxTokens = 2048;
            
            return callModel(modelConfig.getBaseUrl(), modelConfig.getApiKey(), modelName, 
                           systemPrompt, userPrompt, text, temperature, topP, repetitionPenalty, maxTokens);
        } catch (Exception e) {
            log.error("使用智能体配置解析失败", e);
            return Collections.emptyList();
        }
    }

    private List<Map<String, String>> callModel(String baseUrl, String apiKey, String modelName,
                                                  String systemPrompt, String userPrompt, String text,
                                                  BigDecimal temperature, BigDecimal topP,
                                                  BigDecimal repetitionPenalty, Integer maxTokens) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            requestBody.put("stream", false);
            requestBody.put("temperature", temperature.doubleValue());
            requestBody.put("top_p", topP.doubleValue());
            
            if (repetitionPenalty != null) {
                requestBody.put("repeat_penalty", repetitionPenalty.doubleValue());
            }
            
            if (maxTokens != null) {
                requestBody.put("num_predict", maxTokens);
            }
            
            List<Map<String, String>> messages = new ArrayList<>();
            
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            messages.add(systemMessage);
            
            String userContent = userPrompt.replace("{content}", text);
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", userContent);
            messages.add(userMessage);
            
            requestBody.put("messages", messages);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (apiKey != null && !apiKey.isEmpty()) {
                headers.set("Authorization", "Bearer " + apiKey);
            }
            
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            
            log.info("调用模型API: {}/api/chat, 模型: {}", baseUrl, modelName);
            log.info("系统提示词：{}", systemPrompt);
            log.info("用户提示词：{}", userContent);
            
            ResponseEntity<String> response = getRestTemplate().postForEntity(
                baseUrl + "/api/chat", 
                entity, 
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String content = root.path("message").path("content").asText();
                
                log.info("AI返回内容：{}", content);
                
                return parseJsonResult(content);
            }
        } catch (Exception e) {
            log.error("调用模型API失败", e);
        }
        
        return Collections.emptyList();
    }

    private List<Map<String, String>> parseJsonResult(String content) {
        try {
            content = content.trim();
            
            int start = content.indexOf('[');
            if (start < 0) {
                log.error("AI返回内容不是JSON数组格式：{}", content);
                return Collections.emptyList();
            }
            
            int depth = 0;
            int end = -1;
            for (int i = start; i < content.length(); i++) {
                char c = content.charAt(i);
                if (c == '[') depth++;
                else if (c == ']') {
                    depth--;
                    if (depth == 0) {
                        end = i;
                        break;
                    }
                }
            }
            
            if (end <= start) {
                log.error("AI返回内容JSON数组格式不完整：{}", content);
                return Collections.emptyList();
            }
            
            String jsonArray = content.substring(start, end + 1);
            log.info("提取的JSON数组：{}", jsonArray);
            
            List<Map<String, String>> result = objectMapper.readValue(
                jsonArray, 
                new TypeReference<List<Map<String, String>>>() {}
            );
            
            log.info("解析结果数量：{}", result.size());
            return result;
        } catch (Exception e) {
            log.error("解析AI返回的JSON失败：{}", content, e);
            return Collections.emptyList();
        }
    }
}
