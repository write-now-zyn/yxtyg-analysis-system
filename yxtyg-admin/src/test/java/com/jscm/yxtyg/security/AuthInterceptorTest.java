package com.jscm.yxtyg.security;

import com.jscm.yxtyg.service.AuthService;
import com.jscm.yxtyg.service.SysRoleService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthInterceptorTest {

    @AfterEach
    void tearDown() {
        AuthContext.clear();
    }

    @Test
    void clearsAuthContextWhenRoleCheckDeniesRequest() throws Exception {
        CurrentUser user = new CurrentUser();
        user.setId(3L);
        user.setRoleCode(RoleConstants.PRODUCT_MANAGER);

        AuthService authService = mock(AuthService.class);
        when(authService.getUserByToken("token")).thenReturn(user);

        AuthInterceptor interceptor = new AuthInterceptor();
        ReflectionTestUtils.setField(interceptor, "authService", authService);
        ReflectionTestUtils.setField(interceptor, "sysRoleService", mock(SysRoleService.class));

        boolean allowed = interceptor.preHandle(request(), new MockHttpServletResponse(), handler("systemOnly"));

        assertFalse(allowed);
        assertNull(AuthContext.get());
    }

    @Test
    void deniesRequestWhenRoleDoesNotHaveRequiredPermission() throws Exception {
        CurrentUser user = new CurrentUser();
        user.setId(2L);
        user.setRoleCode(RoleConstants.DEV_ADMIN);

        AuthService authService = mock(AuthService.class);
        when(authService.getUserByToken("token")).thenReturn(user);
        SysRoleService roleService = mock(SysRoleService.class);
        when(roleService.listPermissionCodes(anyString())).thenReturn(Collections.emptyList());

        AuthInterceptor interceptor = new AuthInterceptor();
        ReflectionTestUtils.setField(interceptor, "authService", authService);
        ReflectionTestUtils.setField(interceptor, "sysRoleService", roleService);

        boolean allowed = interceptor.preHandle(request(), new MockHttpServletResponse(), handler("manageDemand"));

        assertFalse(allowed);
        assertNull(AuthContext.get());
    }

    private MockHttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token");
        return request;
    }

    private HandlerMethod handler(String methodName) throws NoSuchMethodException {
        Method method = TestController.class.getDeclaredMethod(methodName);
        return new HandlerMethod(new TestController(), method);
    }

    static class TestController {
        @RequireRoles(RoleConstants.SYSTEM_ADMIN)
        public void systemOnly() {
        }

        @RequirePermissions("demand:manage")
        public void manageDemand() {
        }
    }
}
