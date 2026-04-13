package com.jscm.yxtyg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 智能体配置实体类
 */
@Data
@TableName("t_agent_config")
public class AgentConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String agentCode;

    private String agentName;

    private String description;

    private Long modelConfigId;

    private String modelName;

    private BigDecimal temperature;

    private BigDecimal topP;

    private BigDecimal repetitionPenalty;

    private String systemPrompt;

    private String userPrompt;

    private Integer maxTokens;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
