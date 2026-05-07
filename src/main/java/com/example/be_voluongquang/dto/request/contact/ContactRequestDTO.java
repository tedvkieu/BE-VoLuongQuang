package com.example.be_voluongquang.dto.request.contact;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

    @Email(message = "Email không hợp lệ")
    @Size(max = 254, message = "Email không được vượt quá 254 ký tự")
    private String email;

    @NotBlank(message = "Số điện thoại là bắt buộc")
    @Pattern(
        regexp = "^(\\+84|0)[35789][0-9]{8}$",
        message = "Số điện thoại không hợp lệ (VD: 0912345678 hoặc +84912345678)"
    )
    private String phone;

    @NotBlank(message = "Nội dung liên hệ là bắt buộc")
    @Size(max = 4000, message = "Nội dung không được vượt quá 4000 ký tự")
    private String message;
}
