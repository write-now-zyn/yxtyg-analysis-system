package com.jscm.yxtyg.controller;

import com.jscm.yxtyg.common.Result;
import com.jscm.yxtyg.dto.LoginDTO;
import com.jscm.yxtyg.security.AuthContext;
import com.jscm.yxtyg.security.CurrentUser;
import com.jscm.yxtyg.service.AuthService;
import com.jscm.yxtyg.service.SysRoleService;
import com.jscm.yxtyg.vo.AuthVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private SysRoleService sysRoleService;

    @PostMapping("/login")
    public Result<AuthVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success(authService.login(loginDTO));
    }

    @GetMapping("/me")
    public Result<Map<String, Object>> me() {
        CurrentUser user = AuthContext.get();
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("permissions", sysRoleService.listPermissionCodes(user.getRoleCode()));
        return Result.success(result);
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = authorization;
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        authService.logout(token);
        return Result.success();
    }
}
