package com.example.be_voluongquang.dto.response.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {
    private String productId;
    private String name;
    private Double price;
    private Double costPrice;
    private Double wholesalePrice;
    private Integer discountPercent;
    private Integer stockQuantity;
    private Double weight;
    private String unit;
    private Boolean isFeatured;
    private Boolean isActive;
    private String imageUrl;
    private String description;
    private String productGroupId;
    private String categoryId;
    private String brandId;
}

