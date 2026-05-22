package com.jscm.yxtyg.controller;

import com.jscm.yxtyg.common.PageResult;
import com.jscm.yxtyg.common.Result;
import com.jscm.yxtyg.dto.UserDTO;
import com.jscm.yxtyg.dto.UserQueryDTO;
import com.jscm.yxtyg.security.RequireRoles;
import com.jscm.yxtyg.security.RoleConstants;
import com.jscm.yxtyg.service.SysUserService;
import com.jscm.yxtyg.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequireRoles({RoleConstants.SYSTEM_ADMIN})
public class UserController {

    @Autowired
    private SysUserService sysUserService;

    @GetMapping
    public Result<PageResult<UserVO>> list(UserQueryDTO queryDTO) {
        return Result.success(sysUserService.queryPage(queryDTO));
    }

    @GetMapping("/product-managers")
    @RequireRoles({RoleConstants.SYSTEM_ADMIN, RoleConstants.DEV_ADMIN})
    public Result<List<UserVO>> productManagers() {
        return Result.success(sysUserService.listProductManagers());
    }

    @PostMapping
    public Result<UserVO> create(@Valid @RequestBody UserDTO dto) {
        return Result.success(sysUserService.createUser(dto));
    }

    @PutMapping("/{id}")
    public Result<UserVO> update(@PathVariable Long id, @Valid @RequestBody UserDTO dto) {
        return Result.success(sysUserService.updateUser(id, dto));
    }

    @PostMapping("/{id}/disable")
    public Result<Void> disable(@PathVariable Long id) {
        sysUserService.disableUser(id);
        return Result.success();
    }
}
