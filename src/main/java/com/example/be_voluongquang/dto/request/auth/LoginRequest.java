package com.example.be_voluongquang.dto.request.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    private String phone;
    private String email;
    private String password;
}
