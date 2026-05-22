package com.jscm.yxtyg.controller;

import com.jscm.yxtyg.common.Result;
import com.jscm.yxtyg.security.PermissionConstants;
import com.jscm.yxtyg.security.RequirePermissions;
import com.jscm.yxtyg.service.AiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI服务控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequirePermissions(PermissionConstants.DATA_MANAGE)
public class AiController {

    @Autowired
    private AiService aiService;

    /**
     * 解析与会人员文本
     */
    @PostMapping("/parse-participants")
    public Result<List<Map<String, String>>> parseParticipants(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        log.info("收到解析与会人员请求，文本：{}", text);
        
        if (text == null || text.trim().isEmpty()) {
            return Result.error("请输入要解析的文本");
        }
        
        List<Map<String, String>> result = aiService.parseParticipants(text);
        return Result.success(result);
    }

    /**
     * 解析培训记录文本
     */
    @PostMapping("/parse-training")
    public Result<List<Map<String, String>>> parseTrainingRecords(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        log.info("收到解析培训记录请求，文本长度：{}", text != null ? text.length() : 0);
        
        if (text == null || text.trim().isEmpty()) {
            return Result.error("请输入要解析的文本");
        }
        
        List<Map<String, String>> result = aiService.parseTrainingRecords(text);
        return Result.success(result);
    }
}
