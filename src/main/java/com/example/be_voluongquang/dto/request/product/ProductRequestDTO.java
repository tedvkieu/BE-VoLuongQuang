package com.example.be_voluongquang.dto.request.product;

import java.util.List;
import lombok.Data;

@Data
public class ProductRequestDTO {

    private String productId;
    private List<ProductVariantRequestDTO> productVariants;
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
    private String urlShopee;
    private String urlLazada;
    private String urlOther;
    private String description;
    private String brandId;
    private String categoryId;
    private String productGroupId;
}
