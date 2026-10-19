package com.xlj.mall.security;

import java.util.Optional;

/**
 * 当前请求的登录用户，由 AuthInterceptor 写入与清理。
 */
public final class CurrentAdmin {

    private static final ThreadLocal<TokenService.Principal> HOLDER = new ThreadLocal<>();

    private CurrentAdmin() {
    }

    public static void set(TokenService.Principal principal) {
        HOLDER.set(principal);
    }

    public static Optional<TokenService.Principal> get() {
        return Optional.ofNullable(HOLDER.get());
    }

    public static String username() {
        return get().map(TokenService.Principal::username).orElse("system");
    }

    public static void clear() {
        HOLDER.remove();
    }
}
