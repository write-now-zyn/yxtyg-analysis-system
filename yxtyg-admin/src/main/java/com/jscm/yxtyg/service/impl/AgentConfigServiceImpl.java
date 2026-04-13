package com.jscm.yxtyg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jscm.yxtyg.entity.AgentConfig;
import com.jscm.yxtyg.exception.BusinessException;
import com.jscm.yxtyg.mapper.AgentConfigMapper;
import com.jscm.yxtyg.service.AgentConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 智能体配置服务实现类
 */
@Slf4j
@Service
public class AgentConfigServiceImpl extends ServiceImpl<AgentConfigMapper, AgentConfig> implements AgentConfigService {

    @Override
    public List<AgentConfig> listAll() {
        log.debug("获取所有智能体配置");
        LambdaQueryWrapper<AgentConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(AgentConfig::getId);
        return this.list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(AgentConfig config) {
        log.info("更新智能体配置，编码：{}", config.getAgentCode());
        
        AgentConfig existConfig = getByCode(config.getAgentCode());
        if (existConfig == null) {
            throw new BusinessException(400, "智能体配置不存在");
        }
        
        existConfig.setModelConfigId(config.getModelConfigId());
        existConfig.setModelName(config.getModelName());
        existConfig.setTemperature(config.getTemperature());
        existConfig.setTopP(config.getTopP());
        existConfig.setRepetitionPenalty(config.getRepetitionPenalty());
        existConfig.setSystemPrompt(config.getSystemPrompt());
        existConfig.setMaxTokens(config.getMaxTokens());
        existConfig.setUpdateTime(LocalDateTime.now());
        existConfig.setUserPrompt(config.getUserPrompt());
        
        this.updateById(existConfig);
        log.info("更新智能体配置成功");
    }

    @Override
    public AgentConfig getByCode(String agentCode) {
        log.debug("根据编码获取智能体配置：{}", agentCode);
        LambdaQueryWrapper<AgentConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentConfig::getAgentCode, agentCode)
               .last("LIMIT 1");
        return this.getOne(wrapper);
    }
}
