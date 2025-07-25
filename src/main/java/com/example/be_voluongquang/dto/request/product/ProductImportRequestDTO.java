package com.example.be_voluongquang.dto.request.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImportRequestDTO {
    private String productId;
    private String productName;
    private String productGroupId;
    private String categoryId;
    private String brandId;
    private String price;
    private String costPrice;
    private String wholesalePrice;
    private String discountPercent;
    private String stockQuantity;
    private String weight;
    private String unit;
    private Boolean isFeatured;
    private Boolean isActive;
    private String imageUrl;
    private String description;
}
