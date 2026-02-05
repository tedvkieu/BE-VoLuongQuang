package com.example.be_voluongquang.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileUpdateRequest {
    private String fullName;
    private String email;
    private String password;
    private String phone;
    private String address;
}
