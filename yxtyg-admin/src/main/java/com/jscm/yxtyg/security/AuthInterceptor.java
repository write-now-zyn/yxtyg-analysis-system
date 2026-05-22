package com.jscm.yxtyg.security;

import com.alibaba.fastjson2.JSON;
import com.jscm.yxtyg.common.Result;
import com.jscm.yxtyg.service.AuthService;
import com.jscm.yxtyg.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private AuthService authService;

    @Autowired
    private SysRoleService sysRoleService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        AuthContext.clear();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        CurrentUser user = authService.getUserByToken(token);
        if (user == null) {
            writeJson(response, 401, "登录已过期，请重新登录");
            return false;
        }
        AuthContext.set(user);
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            RequireRoles requireRoles = handlerMethod.getMethodAnnotation(RequireRoles.class);
            if (requireRoles == null) {
                requireRoles = handlerMethod.getBeanType().getAnnotation(RequireRoles.class);
            }
            if (requireRoles != null && Arrays.stream(requireRoles.value()).noneMatch(role -> role.equals(user.getRoleCode()))) {
                AuthContext.clear();
                writeJson(response, 403, "无权访问");
                return false;
            }
            RequirePermissions requirePermissions = handlerMethod.getMethodAnnotation(RequirePermissions.class);
            if (requirePermissions == null) {
                requirePermissions = handlerMethod.getBeanType().getAnnotation(RequirePermissions.class);
            }
            if (requirePermissions != null && !hasAnyPermission(user.getRoleCode(), requirePermissions.value())) {
                AuthContext.clear();
                writeJson(response, 403, "无权访问");
                return false;
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }

    private void writeJson(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(200);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSON.toJSONString(Result.error(code, message)));
    }

    private boolean hasAnyPermission(String roleCode, String[] permissionCodes) {
        if (permissionCodes == null || permissionCodes.length == 0) {
            return true;
        }
        List<String> permissions = sysRoleService.listPermissionCodes(roleCode);
        return Arrays.stream(permissionCodes).anyMatch(permissions::contains);
    }
}
