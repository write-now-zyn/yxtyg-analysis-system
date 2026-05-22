package com.jscm.yxtyg.controller;

import com.jscm.yxtyg.common.Result;
import com.jscm.yxtyg.dto.VectorContentRequestDTO;
import com.jscm.yxtyg.security.PermissionConstants;
import com.jscm.yxtyg.security.RequirePermissions;
import com.jscm.yxtyg.service.RecommendService;
import com.jscm.yxtyg.service.VectorStoreService;
import com.jscm.yxtyg.vo.RecommendResultVO;
import com.jscm.yxtyg.vo.VectorMatchResultVO;
import com.jscm.yxtyg.vo.VectorStatsVO;
import com.jscm.yxtyg.vo.VectorSyncResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 智能分单控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/vector")
@RequirePermissions(PermissionConstants.SMART_USE)
public class VectorController {

    @Autowired
    private RecommendService recommendService;

    @Autowired
    private VectorStoreService vectorStoreService;

    @PostMapping("/recommend")
    public Result<RecommendResultVO> recommend(@RequestBody VectorContentRequestDTO requestDTO) {
        log.info("执行智能推荐");
        return Result.success(recommendService.recommend(requestDTO.getContent()));
    }

    @PostMapping("/match")
    public Result<VectorMatchResultVO> match(@RequestBody VectorContentRequestDTO requestDTO) {
        log.info("执行向量搜索");
        VectorMatchResultVO result = new VectorMatchResultVO();
        result.setSimilarTickets(vectorStoreService.match(requestDTO.getContent(), null));
        return Result.success(result);
    }

    @GetMapping("/stats")
    public Result<VectorStatsVO> stats() {
        log.info("查询向量统计");
        return Result.success(vectorStoreService.stats());
    }

    @PostMapping("/sync")
    public Result<VectorSyncResultVO> sync() {
        log.info("执行向量增量同步");
        return Result.success(vectorStoreService.syncIncremental());
    }
}
