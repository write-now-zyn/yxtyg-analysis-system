package com.jscm.yxtyg.controller;

import com.jscm.yxtyg.common.Result;
import com.jscm.yxtyg.entity.ModelConfig;
import com.jscm.yxtyg.security.PermissionConstants;
import com.jscm.yxtyg.security.RequirePermissions;
import com.jscm.yxtyg.service.ModelConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 大模型配置控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/model-config")
@RequirePermissions(PermissionConstants.MODEL_MANAGE)
public class ModelConfigController {

    @Autowired
    private ModelConfigService modelConfigService;

    @GetMapping("/list")
    public Result<List<ModelConfig>> list() {
        log.info("获取大模型配置列表");
        List<ModelConfig> list = modelConfigService.listAll();
        return Result.success(list);
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody ModelConfig config) {
        log.info("新增大模型配置：{}", config.getName());
        modelConfigService.saveConfig(config);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody ModelConfig config) {
        log.info("更新大模型配置，ID：{}", config.getId());
        modelConfigService.saveConfig(config);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除大模型配置，ID：{}", id);
        modelConfigService.deleteConfig(id);
        return Result.success();
    }

    @PostMapping("/test/{id}")
    public Result<Boolean> test(@PathVariable Long id) {
        log.info("测试大模型连接，ID：{}", id);
        boolean success = modelConfigService.testConnection(id);
        return Result.success(success);
    }

    @PostMapping("/test-config")
    public Result<Boolean> testConfig(@RequestBody ModelConfig config) {
        log.info("测试大模型连接，配置：{}", config.getName());
        boolean success = modelConfigService.testConnection(config);
        return Result.success(success);
    }

    @GetMapping("/fetch-models/{id}")
    public Result<List<String>> fetchModels(@PathVariable Long id) {
        log.info("拉取模型列表，ID：{}", id);
        List<String> models = modelConfigService.fetchModels(id);
        return Result.success(models);
    }

    @PostMapping("/set-default/{id}")
    public Result<Void> setDefault(@PathVariable Long id) {
        log.info("设置默认大模型配置，ID：{}", id);
        modelConfigService.setDefaultConfig(id);
        return Result.success();
    }
}
