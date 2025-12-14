package com.example.be_voluongquang.security;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.AntPathMatcher;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String ATTR_AUTHENTICATED_CLAIMS = "authenticatedClaims";
    public static final String ATTR_AUTHENTICATED_USER_ID = "authenticatedUserId";
    public static final String ATTR_AUTHENTICATED_ROLE = "authenticatedUserRole";

    private static final Set<String> PUBLIC_ENDPOINTS = Set.of(
            "/api/auth/login"
    );

    private static final Set<String> PUBLIC_GET_PATTERNS = Set.of(
            "/api/product/**"
    );

    private static final Set<String> ADMIN_PROTECTED_PATTERNS = Set.of(
            "/api/product/**",
            "/api/brand/**",
            "/api/category/**",
            "/api/product-group/**",
            "/api/discount/**",
            "/api/admin/**",
            "/api/user/**"
    );

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        if (path == null) {
            return true;
        }

        if (!path.startsWith("/api/")) {
            return true;
        }

        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        if (PUBLIC_ENDPOINTS.contains(path)) {
            return true;
        }

        if (HttpMethod.GET.matches(request.getMethod()) && isPublicGetPath(path)) {
            return true;
        }

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        String token = extractToken(request);
        if (token == null || token.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized");
            return;
        }

        try {
            Map<String, Object> claims = jwtUtil.parseToken(token);
            request.setAttribute(ATTR_AUTHENTICATED_CLAIMS, claims);

            Object sub = claims.get("sub");
            if (sub instanceof String && !((String) sub).isBlank()) {
                request.setAttribute(ATTR_AUTHENTICATED_USER_ID, sub);
            }

            Object role = claims.get("role");
            if (role instanceof String && !((String) role).isBlank()) {
                String normalizedRole = normalizeRole((String) role);
                request.setAttribute(ATTR_AUTHENTICATED_ROLE, normalizedRole);

                if (requiresPrivileged(request, path) && !isPrivilegedRole(normalizedRole)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("Forbidden: admin or staff required");
                    return;
                }

                UsernamePasswordAuthenticationToken authentication = buildAuthentication(sub, normalizedRole);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token");
            return;
        }

        filterChain.doFilter(request, response);
        SecurityContextHolder.clearContext();
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private boolean isPublicGetPath(String path) {
        return PUBLIC_GET_PATTERNS.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    private boolean requiresPrivileged(HttpServletRequest request, String path) {
        // Only enforce for non-GET on protected patterns
        if (HttpMethod.GET.matches(request.getMethod())) {
            return false;
        }
        return ADMIN_PROTECTED_PATTERNS.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    private boolean isAdminRole(String role) {
        return "ADMIN".equalsIgnoreCase(role);
    }

    private boolean isPrivilegedRole(String role) {
        return "ADMIN".equalsIgnoreCase(role) || "STAFF".equalsIgnoreCase(role);
    }

    private String normalizeRole(String rawRole) {
        if (rawRole == null) {
            return null;
        }
        String normalized = rawRole.trim().toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }
        return normalized;
    }

    private UsernamePasswordAuthenticationToken buildAuthentication(Object principal, String normalizedRole) {
        String role = normalizedRole == null ? "" : normalizedRole.trim().toUpperCase();
        String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                java.util.List.of(new SimpleGrantedAuthority(authority)));
    }
}
