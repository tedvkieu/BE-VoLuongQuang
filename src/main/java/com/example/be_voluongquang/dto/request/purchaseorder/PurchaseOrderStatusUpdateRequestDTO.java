package com.example.be_voluongquang.dto.request.purchaseorder;

import com.example.be_voluongquang.entity.PurchaseOrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderStatusUpdateRequestDTO {
    @NotNull(message = "status is required")
    private PurchaseOrderStatus status;
}

