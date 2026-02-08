package com.example.be_voluongquang.dto.request.purchaseorder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderCreateRequestDTO {

    @NotBlank(message = "Tên khách hàng là bắt buộc")
    @Size(max = 255, message = "Tên khách hàng tối đa 255 ký tự")
    private String customerName;

    @NotBlank(message = "Số điện thoại là bắt buộc")
    @Size(max = 30, message = "Số điện thoại tối đa 30 ký tự")
    private String customerPhone;

    @Email(message = "Email không hợp lệ")
    @Size(max = 320, message = "Email tối đa 320 ký tự")
    private String customerEmail;

    @NotBlank(message = "Địa chỉ là bắt buộc")
    @Size(max = 2000, message = "Địa chỉ tối đa 2000 ký tự")
    private String customerAddress;

    @Valid
    @NotEmpty(message = "Danh sách sản phẩm không được rỗng")
    private List<PurchaseOrderItemCreateRequestDTO> items;
}
