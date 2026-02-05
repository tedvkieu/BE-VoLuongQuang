package com.example.be_voluongquang.security;

public final class ApiAccessRules {
    private ApiAccessRules() {
    }

    public static final String[] PUBLIC_PATTERNS = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/**",
            "/api/product/**",
            "/api/product/search",
            "/api/contact/public",
            "/api/banner/public",
            "/api/category/public",
            "/api/brand/**",
            "/api/product-group/**",
            "/images/**"
    };

    public static final String[] AUTHENTICATED_PATTERNS = {
            "/api/**"
    };
}
