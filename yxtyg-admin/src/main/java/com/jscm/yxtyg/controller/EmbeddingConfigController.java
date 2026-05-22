package com.jscm.yxtyg.controller;

import com.jscm.yxtyg.common.Result;
import com.jscm.yxtyg.entity.EmbeddingConfig;
import com.jscm.yxtyg.security.PermissionConstants;
import com.jscm.yxtyg.security.RequirePermissions;
import com.jscm.yxtyg.service.EmbeddingConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 向量化模型配置控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/embedding-config")
@RequirePermissions(PermissionConstants.MODEL_MANAGE)
public class EmbeddingConfigController {

    @Autowired
    private EmbeddingConfigService embeddingConfigService;

    @GetMapping("/list")
    public Result<List<EmbeddingConfig>> list() {
        return Result.success(embeddingConfigService.listAll());
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody EmbeddingConfig config) {
        embeddingConfigService.saveConfig(config);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody EmbeddingConfig config) {
        embeddingConfigService.saveConfig(config);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        embeddingConfigService.deleteConfig(id);
        return Result.success();
    }

    @PostMapping("/test/{id}")
    public Result<Boolean> test(@PathVariable Long id) {
        return Result.success(embeddingConfigService.testConnection(id));
    }

    @PostMapping("/test-config")
    public Result<Boolean> testConfig(@RequestBody EmbeddingConfig config) {
        return Result.success(embeddingConfigService.testConnection(config));
    }

    @PostMapping("/set-default/{id}")
    public Result<Void> setDefault(@PathVariable Long id) {
        embeddingConfigService.setDefaultConfig(id);
        return Result.success();
    }
}
