package com.example.be_voluongquang.dto.request.product;

import lombok.Data;

@Data
public class ProductVariantRequestDTO {
    private String productVariantId;
    private String variantName;
    private Double variantPrice;
    private Integer stockQuantity;
    private Integer sortOrder;
}
