package com.example.be_voluongquang.dto.response.purchaseorder;

import com.example.be_voluongquang.entity.PurchaseOrderStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderAdminListItemDTO {
    private String purchaseOrderId;
    private String userId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String customerAddress;
    private PurchaseOrderStatus status;
    private Double totalAmount;
    private Double discountAmount;
    private Double finalAmount;
    private LocalDateTime createdAt;
}

