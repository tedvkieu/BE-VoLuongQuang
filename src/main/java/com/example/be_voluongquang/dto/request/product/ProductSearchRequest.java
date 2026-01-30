package com.example.be_voluongquang.dto.request.product;

import java.util.List;

import lombok.Data;

@Data
public class ProductSearchRequest {
    private Integer page;
    private Integer size;
    private String search;
    private List<String> brandIds;
    private List<String> categoryIds;
    private List<String> productGroupIds;
    private Double minPrice;
    private Double maxPrice;
    private Integer minDiscount;
    private Boolean isActive;
    private Boolean isFeatured;
    private Boolean isDeleted;
}
