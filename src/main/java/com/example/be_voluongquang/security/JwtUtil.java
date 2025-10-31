package com.example.be_voluongquang.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-expiration-ms}") long accessExpirationMs,
            @Value("${app.jwt.refresh-expiration-ms}") long refreshExpirationMs) {
        // Use raw UTF-8 bytes of the secret; avoid assuming Base64 to prevent decoding errors
        byte[] raw = secret.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(raw);
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String generateAccessToken(String subject, Map<String, Object> claims) {
        return buildToken(subject, claims, accessExpirationMs);
    }

    public String generateRefreshToken(String subject, Map<String, Object> claims) {
        return buildToken(subject, claims, refreshExpirationMs);
    }

    private String buildToken(String subject, Map<String, Object> claims, long expiresIn) {
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiration = new Date(now + expiresIn);

        Map<String, Object> tokenClaims = new HashMap<>();
        if (claims != null && !claims.isEmpty()) {
            claims.forEach((key, value) -> {
                if (key != null && !key.isBlank() && value != null) {
                    tokenClaims.put(key, value);
                }
            });
        }

        if (subject != null && !subject.isBlank()) {
            tokenClaims.put("id", subject);
        }

        if (!tokenClaims.containsKey("name") && tokenClaims.containsKey("fullName")) {
            tokenClaims.put("name", tokenClaims.get("fullName"));
        }

        var builder = Jwts.builder()
                .setIssuedAt(issuedAt)
                .setExpiration(expiration);

        if (subject != null && !subject.isBlank()) {
            builder.setSubject(subject);
        }

        tokenClaims.forEach(builder::claim);

        return builder.signWith(key, SignatureAlgorithm.HS256).compact();
    }

    public Map<String, Object> parseToken(String token) {
        var claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        // Claims implements Map<String, Object>
        return claims;
    }
}
