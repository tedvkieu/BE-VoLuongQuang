package com.example.be_voluongquang.dto.response.purchaseorder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderItemResponseDTO {
    private String purchaseOrderItemId;
    private String productId;
    private String productVariantId;
    private String variantName;
    private String productName;
    private String productUnit;
    private String productImageUrl;
    private Integer quantity;
    private Double unitPrice;
    private Integer discountPercent;
    private Double finalUnitPrice;
    private Double lineTotal;
    private Double finalLineTotal;
}
