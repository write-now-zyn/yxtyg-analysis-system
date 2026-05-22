package com.jscm.yxtyg.controller;

import com.jscm.yxtyg.common.Result;
import com.jscm.yxtyg.dto.RolePermissionDTO;
import com.jscm.yxtyg.security.PermissionConstants;
import com.jscm.yxtyg.security.RequirePermissions;
import com.jscm.yxtyg.security.RequireRoles;
import com.jscm.yxtyg.security.RoleConstants;
import com.jscm.yxtyg.service.SysRoleService;
import com.jscm.yxtyg.vo.PermissionVO;
import com.jscm.yxtyg.vo.RoleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequireRoles({RoleConstants.SYSTEM_ADMIN})
@RequirePermissions(PermissionConstants.ROLE_MANAGE)
public class RoleController {

    @Autowired
    private SysRoleService sysRoleService;

    @GetMapping
    public Result<List<RoleVO>> list() {
        return Result.success(sysRoleService.listRoles());
    }

    @GetMapping("/permissions")
    public Result<List<PermissionVO>> permissions() {
        return Result.success(sysRoleService.listPermissions());
    }

    @PutMapping("/{code}/permissions")
    public Result<Void> updatePermissions(@PathVariable String code, @RequestBody RolePermissionDTO dto) {
        sysRoleService.updatePermissions(code, dto);
        return Result.success();
    }
}
