package com.example.be_voluongquang.services.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.be_voluongquang.dto.request.auth.LoginRequest;
import com.example.be_voluongquang.dto.request.auth.RegisterRequest;
import com.example.be_voluongquang.dto.response.auth.AuthResponse;
import com.example.be_voluongquang.entity.UserEntity;
import com.example.be_voluongquang.exception.InvalidCredentialsException;
import com.example.be_voluongquang.exception.PhoneAlreadyExistsException;
import com.example.be_voluongquang.exception.UserAlreadyExistsException;
import com.example.be_voluongquang.repository.UserRepository;
import com.example.be_voluongquang.security.JwtUtil;
import com.example.be_voluongquang.services.AuthService;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public AuthResponse login(LoginRequest request) {
        String rawPhone = normalizePhone(request.getPhone());
        String rawEmail = request.getEmail() == null ? "" : request.getEmail().trim();
        String rawPassword = request.getPassword() == null ? "" : request.getPassword();

        UserEntity user;
        if (StringUtils.hasText(rawPhone)) {
            user = userRepository.findByPhoneAndIsDeletedFalse(rawPhone)
                    .orElseThrow(() -> new InvalidCredentialsException("Số điện thoại không tồn tại trong hệ thống"));
        } else if (StringUtils.hasText(rawEmail)) {
            user = userRepository.findByEmailIgnoreCaseAndIsDeletedFalse(rawEmail)
                    .orElseThrow(() -> new InvalidCredentialsException("Email không tồn tại trong hệ thống"));
        } else {
            throw new InvalidCredentialsException("Số điện thoại và mật khẩu không được để trống");
        }

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new InvalidCredentialsException("Mật khẩu không chính xác");
        }

        Map<String, Object> claims = new HashMap<>();
        String role = user.getRole();
        if (role == null || role.isBlank()) {
            role = "CUSTOMER";
        }
        claims.put("role", role);
        if (StringUtils.hasText(user.getEmail())) {
            claims.put("email", user.getEmail());
        }
        if (StringUtils.hasText(user.getPhone())) {
            claims.put("phone", user.getPhone());
        }
        if (user.getFullName() != null && !user.getFullName().isBlank()) {
            claims.put("fullName", user.getFullName());

        }
        claims.put("userId", user.getUserId());

        String access = jwtUtil.generateAccessToken(user.getUserId(), claims);
        String refresh = jwtUtil.generateRefreshToken(user.getUserId(), claims);

        return AuthResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(role)
                .build();
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        String rawPhone = normalizePhone(request.getPhone());
        String rawEmail = request.getEmail() == null ? "" : request.getEmail().trim();
        String rawPassword = request.getPassword() == null ? "" : request.getPassword();
        String rawFullName = request.getFullName() == null ? "" : request.getFullName().trim();
        String rawAddress = request.getAddress() == null ? "" : request.getAddress().trim();

        if (!StringUtils.hasText(rawPhone) || !StringUtils.hasText(rawPassword)) {
            throw new InvalidCredentialsException("Số điện thoại và mật khẩu không được để trống");
        }

        if (userRepository.existsByPhoneAndIsDeletedFalse(rawPhone)) {
            throw new PhoneAlreadyExistsException(rawPhone);
        }
        if (StringUtils.hasText(rawEmail) && userRepository.existsByEmailIgnoreCaseAndIsDeletedFalse(rawEmail)) {
            throw new UserAlreadyExistsException(rawEmail);
        }

        String role = request.getRole();
        if (!StringUtils.hasText(role)) {
            role = "CUSTOMER";
        }
        role = role.trim().toUpperCase();

        UserEntity user = new UserEntity();
        user.setEmail(StringUtils.hasText(rawEmail) ? rawEmail : null);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setFullName(StringUtils.hasText(rawFullName) ? rawFullName : rawPhone);
        user.setPhone(rawPhone);
        user.setAddress(StringUtils.hasText(rawAddress) ? rawAddress : null);
        user.setRole(role);

        UserEntity saved = userRepository.save(user);

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        if (StringUtils.hasText(saved.getEmail())) {
            claims.put("email", saved.getEmail());
        }
        if (StringUtils.hasText(saved.getPhone())) {
            claims.put("phone", saved.getPhone());
        }
        if (StringUtils.hasText(saved.getFullName())) {
            claims.put("fullName", saved.getFullName());
        }
        claims.put("userId", saved.getUserId());

        String access = jwtUtil.generateAccessToken(saved.getUserId(), claims);
        String refresh = jwtUtil.generateRefreshToken(saved.getUserId(), claims);

        return AuthResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .userId(saved.getUserId())
                .fullName(saved.getFullName())
                .email(saved.getEmail())
                .role(role)
                .build();
    }

    private String normalizePhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return "";
        }
        String digits = phone.trim().replaceAll("[^0-9]", "");
        if (digits.startsWith("84") && digits.length() == 11) {
            digits = "0" + digits.substring(2);
        }
        return digits;
    }
}
