package com.example.be_voluongquang.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRoleUpdateRequest {
    @NotBlank(message = "Role is required")
    private String role;
}
