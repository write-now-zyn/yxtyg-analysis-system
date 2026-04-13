package com.jscm.yxtyg.controller;

import com.jscm.yxtyg.common.Result;
import com.jscm.yxtyg.entity.AgentConfig;
import com.jscm.yxtyg.service.AgentConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 智能体配置控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/agent-config")
public class AgentConfigController {

    @Autowired
    private AgentConfigService agentConfigService;

    @GetMapping("/list")
    public Result<List<AgentConfig>> list() {
        log.info("获取智能体配置列表");
        List<AgentConfig> list = agentConfigService.listAll();
        return Result.success(list);
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody AgentConfig config) {
        log.info("更新智能体配置，编码：{}", config.getAgentCode());
        agentConfigService.updateConfig(config);
        return Result.success();
    }

    @GetMapping("/detail/{code}")
    public Result<AgentConfig> detail(@PathVariable String code) {
        log.info("获取智能体配置详情，编码：{}", code);
        AgentConfig config = agentConfigService.getByCode(code);
        return Result.success(config);
    }
}
