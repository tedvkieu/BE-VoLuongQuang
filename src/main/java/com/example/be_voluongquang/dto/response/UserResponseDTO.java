package com.example.be_voluongquang.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private String userId;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String role;
    private String createdAt;
}
