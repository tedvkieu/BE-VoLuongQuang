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
import com.example.be_voluongquang.entity.ProductVariantEntity;
import com.example.be_voluongquang.entity.UserEntity;
import com.example.be_voluongquang.exception.ResourceNotFoundException;
import com.example.be_voluongquang.repository.CartItemRepository;
import com.example.be_voluongquang.repository.CartRepository;
import com.example.be_voluongquang.repository.ProductRepository;
import com.example.be_voluongquang.repository.ProductVariantRepository;
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
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;

    private String normalizeVariantId(String raw) {
        if (raw == null) return null;
        String value = raw.trim();
        return value.isEmpty() ? null : value;
    }

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
        String productVariantId = normalizeVariantId(request.getProductVariantId());
        int quantity = request.getQuantity() == null ? 1 : request.getQuantity();
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn hoặc bằng 1");
        }

        String productId = request.getProductId() == null ? null : request.getProductId().trim();
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("productId không hợp lệ");
        }

        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", request.getProductId()));

        ProductVariantEntity variant = null;
        if (productVariantId != null) {
            final String requestedVariantId = productVariantId;
            variant = productVariantRepository.findById(requestedVariantId)
                    .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "productVariantId", requestedVariantId));
            if (Boolean.TRUE.equals(variant.getIsDeleted())) {
                throw new IllegalStateException("Phân loại sản phẩm đã bị tắt");
            }
            String variantProductId =
                    variant.getProduct() != null ? variant.getProduct().getProductId() : null;
            if (variantProductId == null || !variantProductId.equals(productId)) {
                throw new IllegalArgumentException("Phân loại sản phẩm không thuộc sản phẩm đã chọn");
            }
        } else {
            List<ProductVariantEntity> variants =
                    productVariantRepository.findByProductProductIdAndIsDeletedFalseOrderBySortOrderAsc(productId);
            if (variants != null && !variants.isEmpty()) {
                variant = variants.get(0);
                productVariantId = variant.getProductVariantId();
            }
        }

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
                .findByCartIdAndProductIdAndVariantId(cart.getCartId(), product.getProductId(), productVariantId)
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
                    .productVariantId(productVariantId)
                    .cart(cart)
                    .product(product)
                    .productVariant(variant)
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
        String productVariantId = normalizeVariantId(request.getProductVariantId());
        int targetQuantity = request.getQuantity() == null ? 0 : request.getQuantity();
        if (targetQuantity < 0) {
            throw new IllegalArgumentException("Số lượng không hợp lệ");
        }

        if (targetQuantity == 0) {
            return removeItemFromCart(userId, request.getProductId(), productVariantId);
        }

        CartEntity cart = cartRepository.findCartWithItemsByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", userId));

        String productId = request.getProductId() == null ? null : request.getProductId().trim();
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("productId không hợp lệ");
        }

        CartItemEntity cartItem = cart.getCartItems().stream()
                .filter(item -> item != null
                        && productId.equals(item.getProductId())
                        && ((productVariantId == null && item.getProductVariantId() == null)
                        || (productVariantId != null && productVariantId.equals(item.getProductVariantId()))))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "productId", productId));

        ProductEntity product = cartItem.getProduct();
        if (product == null) {
            product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
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
    public CartResponseDTO removeItemFromCart(String userId, String productId, String productVariantId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("Không xác định được người dùng");
        }
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Sản phẩm không hợp lệ");
        }
        String normalizedProductId = productId.trim();
        String normalizedVariantId = normalizeVariantId(productVariantId);

        CartEntity cart = cartRepository.findCartWithItemsAndProductDetailsByUserId(userId)
                .orElse(null);

        if (cart == null) {
            return emptyCartResponse();
        }

        boolean existsInCart = cart.getCartItems().stream()
                .anyMatch(item -> item != null
                        && normalizedProductId.equals(item.getProductId())
                        && ((normalizedVariantId == null && item.getProductVariantId() == null)
                        || (normalizedVariantId != null && normalizedVariantId.equals(item.getProductVariantId()))));

        if (!existsInCart) {
            return mapToCartResponse(cart);
        }

        cartItemRepository.deleteByCartIdAndProductIdAndVariantId(
                cart.getCartId(),
                normalizedProductId,
                normalizedVariantId);
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
            ProductVariantEntity variant = cartItem.getProductVariant();
            double price =
                    variant != null
                            ? (variant.getVariantPrice() == null ? 0.0 : variant.getVariantPrice())
                            : (product.getPrice() == null ? 0.0 : product.getPrice());
            int discountPercent = product.getDiscountPercent() == null ? 0 : product.getDiscountPercent();
            double discountedPrice =
                    variant != null && variant.getFinalPrice() != null && variant.getFinalPrice() >= 0
                            ? variant.getFinalPrice()
                            : price * (1 - discountPercent / 100.0);

            double subtotalOriginal = price * quantity;
            double subtotalDiscounted = discountedPrice * quantity;

            totalQuantity += quantity;
            totalOriginalPrice += subtotalOriginal;
            totalDiscountedPrice += subtotalDiscounted;

            items.add(CartItemResponseDTO.builder()
                    .productId(product.getProductId())
                    .productVariantId(cartItem.getProductVariantId())
                    .variantName(variant != null ? variant.getVariantName() : null)
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
