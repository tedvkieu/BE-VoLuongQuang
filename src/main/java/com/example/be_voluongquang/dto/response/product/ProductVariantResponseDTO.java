package com.example.be_voluongquang.dto.response.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantResponseDTO {
    private String productVariantId;
    private String variantName;
    private Double variantPrice;
    private Double finalPrice;
    private Integer stockQuantity;
    private Integer sortOrder;
    private Boolean isDeleted;
}
