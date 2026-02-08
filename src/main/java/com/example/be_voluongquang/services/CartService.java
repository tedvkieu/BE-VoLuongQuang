package com.example.be_voluongquang.services;

import com.example.be_voluongquang.dto.request.cart.CartItemRequestDTO;
import com.example.be_voluongquang.dto.request.cart.CartItemUpdateRequestDTO;
import com.example.be_voluongquang.dto.response.cart.CartResponseDTO;

public interface CartService {

    CartResponseDTO getCurrentUserCart(String userId);

    CartResponseDTO addItemToCart(String userId, CartItemRequestDTO request);

    CartResponseDTO updateItemQuantity(String userId, CartItemUpdateRequestDTO request);

    CartResponseDTO removeItemFromCart(String userId, String productId, String productVariantId);

    CartResponseDTO getCartByUserId(String userId);
}
