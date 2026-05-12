package com.jscm.yxtyg.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 智能推荐配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "recommend")
public class RecommendProperties {

    private Integer topK = 10;

    private Integer maxPromptTickets = 10;

    private Integer maxTicketContentLength = 2000;

    private Integer maxCurrentContentLength = 500;

    private Integer maxHistoryContentLength = 200;

    private Integer maxHistorySolutionLength = 300;

    private Integer llmTimeoutMs = 300000;
}
