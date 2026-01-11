package com.example.be_voluongquang.services.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.be_voluongquang.dto.request.cart.CartItemRequestDTO;
import com.example.be_voluongquang.dto.response.cart.CartItemResponseDTO;
import com.example.be_voluongquang.dto.response.cart.CartResponseDTO;
import com.example.be_voluongquang.entity.CartEntity;
import com.example.be_voluongquang.entity.CartItemEntity;
import com.example.be_voluongquang.entity.ProductEntity;
import com.example.be_voluongquang.entity.UserEntity;
import com.example.be_voluongquang.exception.ResourceNotFoundException;
import com.example.be_voluongquang.repository.CartItemRepository;
import com.example.be_voluongquang.repository.CartRepository;
import com.example.be_voluongquang.repository.ProductRepository;
import com.example.be_voluongquang.repository.UserRepository;
import com.example.be_voluongquang.services.CartService;
import com.example.be_voluongquang.dto.request.cart.CartItemUpdateRequestDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public CartResponseDTO getCurrentUserCart(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("Không xác định được người dùng");
        }

        return cartRepository.findCartWithItemsByUserId(userId)
                .map(this::mapToCartResponse)
                .orElseGet(this::emptyCartResponse);
    }

    @Override
    @Transactional
    public CartResponseDTO addItemToCart(String userId, CartItemRequestDTO request) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("Không xác định được người dùng");
        }
        if (request == null) {
            throw new IllegalArgumentException("Yêu cầu không hợp lệ");
        }
        int quantity = request.getQuantity() == null ? 1 : request.getQuantity();
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn hoặc bằng 1");
        }

        ProductEntity product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", request.getProductId()));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

        CartEntity cart = user.getCart();
        if (cart == null) {
            cart = CartEntity.builder()
                    .user(user)
                    .cartItems(new ArrayList<>())
                    .build();
            cart = cartRepository.save(cart);
            user.setCart(cart);
        }

        CartItemEntity cartItem = cartItemRepository
                .findByCartIdAndProductId(cart.getCartId(), product.getProductId())
                .orElse(null);

        // int availableStock = product.getStockQuantity() == null ? Integer.MAX_VALUE :
        // product.getStockQuantity();

        if (cartItem == null) {
            // if (quantity > availableStock) {
            // throw new IllegalArgumentException("Số lượng sản phẩm vượt quá tồn kho");
            // }
            cartItem = CartItemEntity.builder()
                    .cartId(cart.getCartId())
                    .productId(product.getProductId())
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .build();
        } else {
            int updatedQuantity = (cartItem.getQuantity() == null ? 0 : cartItem.getQuantity()) + quantity;

            cartItem.setQuantity(updatedQuantity);
        }

        cartItemRepository.save(cartItem);
        cartRepository.flush();

        return cartRepository.findCartWithItemsByUserId(userId)
                .map(this::mapToCartResponse)
                .orElseGet(this::emptyCartResponse);
    }

    @Override
    @Transactional
    public CartResponseDTO updateItemQuantity(String userId, CartItemUpdateRequestDTO request) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("Không xác định được người dùng");
        }
        if (request == null) {
            throw new IllegalArgumentException("Yêu cầu không hợp lệ");
        }
        int targetQuantity = request.getQuantity() == null ? 0 : request.getQuantity();
        if (targetQuantity < 0) {
            throw new IllegalArgumentException("Số lượng không hợp lệ");
        }

        if (targetQuantity == 0) {
            return removeItemFromCart(userId, request.getProductId());
        }

        CartEntity cart = cartRepository.findCartWithItemsByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", userId));

        CartItemEntity cartItem = cart.getCartItems().stream()
                .filter(item -> item != null && request.getProductId().equals(item.getProductId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "productId", request.getProductId()));

        ProductEntity product = cartItem.getProduct();
        if (product == null) {
            product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", request.getProductId()));
            cartItem.setProduct(product);
        }

        int availableStock = product.getStockQuantity() == null ? Integer.MAX_VALUE : product.getStockQuantity();
        // if (targetQuantity > availableStock) {
        // throw new IllegalArgumentException("Số lượng sản phẩm vượt quá tồn kho");
        // }

        cartItem.setQuantity(targetQuantity);
        cartItemRepository.save(cartItem);
        cartRepository.flush();

        return cartRepository.findCartWithItemsByUserId(userId)
                .map(this::mapToCartResponse)
                .orElseGet(this::emptyCartResponse);
    }

    @Override
    @Transactional
    public CartResponseDTO removeItemFromCart(String userId, String productId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("Không xác định được người dùng");
        }
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Sản phẩm không hợp lệ");
        }

        CartEntity cart = cartRepository.findCartWithItemsAndProductDetailsByUserId(userId)
                .orElse(null);

        if (cart == null) {
            return emptyCartResponse();
        }

        boolean existsInCart = cart.getCartItems().stream()
                .anyMatch(item -> item != null && productId.equals(item.getProductId()));

        if (!existsInCart) {
            return mapToCartResponse(cart);
        }

        cartItemRepository.deleteByCartIdAndProductId(cart.getCartId(), productId);
        cartRepository.flush();

        return cartRepository.findCartWithItemsByUserId(userId)
                .map(this::mapToCartResponse)
                .orElseGet(this::emptyCartResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponseDTO getCartByUserId(String userId) {
        return getCurrentUserCart(userId);
    }

    private CartResponseDTO emptyCartResponse() {
        return CartResponseDTO.builder()
                .items(Collections.emptyList())
                .totalQuantity(0)
                .totalOriginalPrice(0.0)
                .totalDiscountedPrice(0.0)
                .totalSavings(0.0)
                .build();
    }

    private CartResponseDTO mapToCartResponse(CartEntity cart) {
        if (cart == null || cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            return emptyCartResponse();
        }

        List<CartItemResponseDTO> items = new ArrayList<>();
        double totalOriginalPrice = 0.0;
        double totalDiscountedPrice = 0.0;
        int totalQuantity = 0;

        for (CartItemEntity cartItem : cart.getCartItems()) {
            if (cartItem == null) {
                continue;
            }
            ProductEntity product = cartItem.getProduct();
            if (product == null) {
                continue;
            }

            int quantity = cartItem.getQuantity() == null ? 0 : cartItem.getQuantity();
            double price = product.getPrice() == null ? 0.0 : product.getPrice();
            int discountPercent = product.getDiscountPercent() == null ? 0 : product.getDiscountPercent();
            double discountedPrice = price * (1 - discountPercent / 100.0);

            double subtotalOriginal = price * quantity;
            double subtotalDiscounted = discountedPrice * quantity;

            totalQuantity += quantity;
            totalOriginalPrice += subtotalOriginal;
            totalDiscountedPrice += subtotalDiscounted;

            items.add(CartItemResponseDTO.builder()
                    .productId(product.getProductId())
                    .name(product.getName())
                    .imageUrl(product.getImageUrl())
                    .price(price)
                    .discountPercent(discountPercent)
                    .discountedPrice(discountedPrice)
                    .quantity(quantity)
                    .subtotalOriginalPrice(subtotalOriginal)
                    .subtotalDiscountedPrice(subtotalDiscounted)
                    .build());
        }

        double totalSavings = totalOriginalPrice - totalDiscountedPrice;

        return CartResponseDTO.builder()
                .items(items)
                .totalQuantity(totalQuantity)
                .totalOriginalPrice(totalOriginalPrice)
                .totalDiscountedPrice(totalDiscountedPrice)
                .totalSavings(totalSavings)
                .build();
    }
}
