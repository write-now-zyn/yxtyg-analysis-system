package com.jscm.yxtyg.security;

/**
 * Request-scoped authentication context.
 */
public final class AuthContext {

    private static final ThreadLocal<CurrentUser> CURRENT_USER = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(CurrentUser user) {
        CURRENT_USER.set(user);
    }

    public static CurrentUser get() {
        return CURRENT_USER.get();
    }

    public static Long currentUserId() {
        CurrentUser user = get();
        return user == null ? null : user.getId();
    }

    public static boolean hasRole(String roleCode) {
        CurrentUser user = get();
        return user != null && roleCode != null && roleCode.equals(user.getRoleCode());
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
