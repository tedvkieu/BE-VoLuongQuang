package com.example.be_voluongquang.dto.response.cart;

import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponseDTO {
    @Builder.Default
    private List<CartItemResponseDTO> items = Collections.emptyList();

    @Builder.Default
    private Integer totalQuantity = 0;

    @Builder.Default
    private Double totalOriginalPrice = 0.0;

    @Builder.Default
    private Double totalDiscountedPrice = 0.0;

    @Builder.Default
    private Double totalSavings = 0.0;
}
