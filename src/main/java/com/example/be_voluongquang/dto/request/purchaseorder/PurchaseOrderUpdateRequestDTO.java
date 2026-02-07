package com.example.be_voluongquang.dto.request.purchaseorder;

import jakarta.validation.Valid;
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
public class PurchaseOrderUpdateRequestDTO {
    @Size(max = 255, message = "customerName too long")
    private String customerName;

    @Size(max = 30, message = "customerPhone too long")
    private String customerPhone;

    @Size(max = 320, message = "customerEmail too long")
    private String customerEmail;

    @Size(max = 2000, message = "customerAddress too long")
    private String customerAddress;

    @Valid
    private List<PurchaseOrderItemUpdateRequestDTO> items;
}

