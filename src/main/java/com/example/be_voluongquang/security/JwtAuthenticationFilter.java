package com.example.be_voluongquang.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

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

        if (isPublicRequest(request)) {
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

    private boolean hasAccessTokenCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return false;
        }
        for (Cookie cookie : request.getCookies()) {
            if ("accessToken".equals(cookie.getName())) {
                return true;
            }
        }
        return false;
    }

    private boolean isPublicRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path == null) {
            return false;
        }
        return matchesAny(path, ApiAccessRules.PUBLIC_PATTERNS);
    }

    private boolean matchesAny(String path, String[] patterns) {
        if (patterns == null || patterns.length == 0) {
            return false;
        }
        return Arrays.stream(patterns).anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
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
