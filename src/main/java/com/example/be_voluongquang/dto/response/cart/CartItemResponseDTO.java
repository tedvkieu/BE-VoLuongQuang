package com.example.be_voluongquang.dto.response.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponseDTO {
    private String productId;
    private String name;
    private String imageUrl;
    private Double price;
    private Integer discountPercent;
    private Double discountedPrice;
    private Integer quantity;
    private Double subtotalOriginalPrice;
    private Double subtotalDiscountedPrice;
}
