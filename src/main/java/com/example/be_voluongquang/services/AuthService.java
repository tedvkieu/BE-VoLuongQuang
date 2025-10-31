package com.example.be_voluongquang.services;

import com.example.be_voluongquang.dto.request.auth.LoginRequest;
import com.example.be_voluongquang.dto.response.auth.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
}

