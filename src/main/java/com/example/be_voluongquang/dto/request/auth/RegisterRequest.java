package com.example.be_voluongquang.dto.request.auth;

import lombok.Data;

@Data
public class RegisterRequest {
    private String fullName;
    private String email;
    private String password;
    private String phone;
    private String address;
    /**
     * Vai trò mong muốn, mặc định CUSTOMER nếu không truyền hoặc trống.
     */
    private String role;
}
