package com.example.be_voluongquang.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.be_voluongquang.dto.request.cart.CartItemRequestDTO;
import com.example.be_voluongquang.dto.request.cart.CartItemUpdateRequestDTO;
import com.example.be_voluongquang.dto.response.cart.CartResponseDTO;
import com.example.be_voluongquang.security.JwtAuthenticationFilter;
import com.example.be_voluongquang.services.CartService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping(path = "/api/cart", produces = MediaType.APPLICATION_JSON_VALUE)
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponseDTO> getCurrentUserCart(HttpServletRequest request) {
        String userId = resolveUserId(request);
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        CartResponseDTO cart = cartService.getCurrentUserCart(userId);
        return ResponseEntity.ok(cart);
    }

    @PostMapping(path = "/items", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CartResponseDTO> addItemToCart(
            @Valid @RequestBody CartItemRequestDTO requestBody,
            HttpServletRequest request) {
        String userId = resolveUserId(request);
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).build();
        }

        CartResponseDTO cart = cartService.addItemToCart(userId, requestBody);
        return ResponseEntity.ok(cart);
    }

    @PatchMapping(path = "/items", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CartResponseDTO> updateItemQuantity(
            @Valid @RequestBody CartItemUpdateRequestDTO requestBody,
            HttpServletRequest request) {
        String userId = resolveUserId(request);
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).build();
        }

        CartResponseDTO cart = cartService.updateItemQuantity(userId, requestBody);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping(path = "/items/{productId}")
    public ResponseEntity<CartResponseDTO> removeItemFromCart(
            @PathVariable("productId") String productId,
            HttpServletRequest request) {
        String userId = resolveUserId(request);
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).build();
        }

        CartResponseDTO cart = cartService.removeItemFromCart(userId, productId);
        return ResponseEntity.ok(cart);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<CartResponseDTO> getCartByUserId(
            @PathVariable("userId") String targetUserId,
            HttpServletRequest request) {
        String authenticatedUserId = resolveUserId(request);
        if (authenticatedUserId == null || authenticatedUserId.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        if (!authenticatedUserId.equals(targetUserId)) {
            return ResponseEntity.status(403).build();
        }
        CartResponseDTO cart = cartService.getCartByUserId(targetUserId);
        return ResponseEntity.ok(cart);
    }

    private String resolveUserId(HttpServletRequest request) {
        Object attr = request.getAttribute(JwtAuthenticationFilter.ATTR_AUTHENTICATED_USER_ID);
        return attr instanceof String attrValue ? attrValue : null;
    }
}
