package com.example.be_voluongquang.dto.request.contact;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactRequestDTO {

    @Email(message = "Email is invalid")
    private String email;

    @Size(max = 30, message = "Phone must be at most 30 characters")
    private String phone;

    @NotBlank(message = "Message is required")
    @Size(max = 4000, message = "Message must be at most 4000 characters")
    private String message;

    @AssertTrue(message = "Email hoặc số điện thoại là bắt buộc")
    public boolean isEmailOrPhoneProvided() {
        return hasText(email) || hasText(phone);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
