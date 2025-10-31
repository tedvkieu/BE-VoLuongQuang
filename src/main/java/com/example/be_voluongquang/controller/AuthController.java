package com.example.be_voluongquang.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.be_voluongquang.dto.request.auth.LoginRequest;
import com.example.be_voluongquang.dto.response.auth.AuthResponse;
import com.example.be_voluongquang.dto.response.auth.UserProfileResponse;
import com.example.be_voluongquang.repository.UserRepository;
import com.example.be_voluongquang.security.JwtAuthenticationFilter;
import com.example.be_voluongquang.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private com.example.be_voluongquang.services.AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse res = authService.login(request);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        String userId = resolveUserId(request);
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        try {
            var profileOpt = userRepository.findUserWithCartAndItems(userId)
                    .map(user -> {
                        int cartItemCount = 0;
                        if (user.getCart() != null && user.getCart().getCartItems() != null) {
                            cartItemCount = user.getCart().getCartItems().stream()
                                    .map(item -> item.getQuantity() == null ? 0 : item.getQuantity())
                                    .mapToInt(Integer::intValue)
                                    .sum();
                        }
                        return UserProfileResponse.builder()
                                .userId(user.getUserId())
                                .fullName(user.getFullName())
                                .email(user.getEmail())
                                .role(user.getRole())
                                .cartItemCount(cartItemCount)
                                .build();
                    });

            if (profileOpt.isPresent()) {
                return ResponseEntity.ok(profileOpt.get());
            }
            return ResponseEntity.status(404).body("User not found");
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid token");
        }
    }

    private String resolveUserId(HttpServletRequest request) {
        Object attr = request.getAttribute(JwtAuthenticationFilter.ATTR_AUTHENTICATED_USER_ID);
        if (attr instanceof String attrValue && !attrValue.isBlank()) {
            return attrValue;
        }

        String token = extractTokenFromRequest(request);
        if (token == null || token.isBlank()) {
            return null;
        }

        try {
            var claims = jwtUtil.parseToken(token);
            Object sub = claims.get("sub");
            if (sub instanceof String subValue && !subValue.isBlank()) {
                return subValue;
            }
        } catch (Exception ex) {
            return null;
        }

        return null;
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
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
}
