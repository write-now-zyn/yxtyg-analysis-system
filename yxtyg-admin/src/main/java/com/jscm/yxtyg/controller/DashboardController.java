package com.jscm.yxtyg.controller;

import com.jscm.yxtyg.common.Result;
import com.jscm.yxtyg.security.PermissionConstants;
import com.jscm.yxtyg.security.RequirePermissions;
import com.jscm.yxtyg.service.DashboardService;
import com.jscm.yxtyg.vo.DashboardVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 看板控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequirePermissions(PermissionConstants.DASHBOARD_VIEW)
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * 获取看板概览数据
     */
    @GetMapping("/overview")
    public Result<DashboardVO> overview(@RequestParam(required = false) String month) {
        log.info("获取看板概览数据，月份：{}", month);
        DashboardVO result = dashboardService.getOverview(month);
        return Result.success(result);
    }
}
