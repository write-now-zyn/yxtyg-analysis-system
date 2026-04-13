package com.jscm.yxtyg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jscm.yxtyg.entity.AgentConfig;

import java.util.List;

/**
 * 智能体配置服务接口
 */
public interface AgentConfigService extends IService<AgentConfig> {

    List<AgentConfig> listAll();

    void updateConfig(AgentConfig config);

    AgentConfig getByCode(String agentCode);
}
