package com.jscm.yxtyg.controller;

import com.jscm.yxtyg.common.PageResult;
import com.jscm.yxtyg.common.Result;
import com.jscm.yxtyg.dto.DemandWorkloadDTO;
import com.jscm.yxtyg.dto.DemandWorkloadQueryDTO;
import com.jscm.yxtyg.dto.FinalWorkloadDTO;
import com.jscm.yxtyg.dto.ReminderDTO;
import com.jscm.yxtyg.security.PermissionConstants;
import com.jscm.yxtyg.security.RequirePermissions;
import com.jscm.yxtyg.service.DemandWorkloadService;
import com.jscm.yxtyg.vo.DemandWorkloadStatisticsVO;
import com.jscm.yxtyg.vo.DemandWorkloadVO;
import com.jscm.yxtyg.vo.ImportResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/demand-workloads")
public class DemandWorkloadController {

    @Autowired
    private DemandWorkloadService demandWorkloadService;

    @GetMapping
    @RequirePermissions(PermissionConstants.DEMAND_VIEW)
    public Result<PageResult<DemandWorkloadVO>> list(DemandWorkloadQueryDTO queryDTO) {
        return Result.success(demandWorkloadService.queryPage(queryDTO));
    }

    @GetMapping("/{id}")
    @RequirePermissions(PermissionConstants.DEMAND_VIEW)
    public Result<DemandWorkloadVO> detail(@PathVariable Long id) {
        return Result.success(demandWorkloadService.getDetail(id));
    }

    @PostMapping
    @RequirePermissions(PermissionConstants.DEMAND_MANAGE)
    public Result<DemandWorkloadVO> create(@Valid @RequestBody DemandWorkloadDTO dto) {
        return Result.success(demandWorkloadService.create(dto));
    }

    @PutMapping("/{id}")
    @RequirePermissions(PermissionConstants.DEMAND_MANAGE)
    public Result<DemandWorkloadVO> update(@PathVariable Long id, @Valid @RequestBody DemandWorkloadDTO dto) {
        return Result.success(demandWorkloadService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @RequirePermissions(PermissionConstants.DEMAND_MANAGE)
    public Result<Void> delete(@PathVariable Long id) {
        demandWorkloadService.deleteDemand(id);
        return Result.success();
    }

    @PostMapping("/import")
    @RequirePermissions(PermissionConstants.DEMAND_MANAGE)
    public Result<ImportResultVO> importExcel(@RequestParam("file") MultipartFile file) {
        return Result.success(demandWorkloadService.importExcel(file));
    }

    @GetMapping("/template")
    @RequirePermissions(PermissionConstants.DEMAND_MANAGE)
    public void template(HttpServletResponse response) {
        demandWorkloadService.downloadTemplate(response);
    }

    @GetMapping("/export")
    @RequirePermissions(PermissionConstants.DEMAND_VIEW)
    public void export(DemandWorkloadQueryDTO queryDTO, HttpServletResponse response) {
        demandWorkloadService.exportExcel(queryDTO, response);
    }

    @PostMapping("/{id}/submit-final")
    @RequirePermissions(PermissionConstants.DEMAND_FILL)
    public Result<DemandWorkloadVO> submitFinal(@PathVariable Long id, @Valid @RequestBody FinalWorkloadDTO dto) {
        return Result.success(demandWorkloadService.submitFinal(id, dto));
    }

    @PostMapping("/{id}/confirm")
    @RequirePermissions(PermissionConstants.DEMAND_CONFIRM)
    public Result<DemandWorkloadVO> confirm(@PathVariable Long id) {
        return Result.success(demandWorkloadService.confirm(id));
    }

    @PostMapping("/{id}/reminders")
    @RequirePermissions(PermissionConstants.DEMAND_REMIND)
    public Result<Void> remind(@PathVariable Long id, @RequestBody ReminderDTO dto) {
        demandWorkloadService.remind(id, dto);
        return Result.success();
    }

    @GetMapping("/statistics")
    @RequirePermissions(PermissionConstants.DEMAND_VIEW)
    public Result<DemandWorkloadStatisticsVO> statistics(DemandWorkloadQueryDTO queryDTO) {
        return Result.success(demandWorkloadService.statistics(queryDTO));
    }
}
