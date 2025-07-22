package com.example.be_voluongquang.repository;

import com.example.be_voluongquang.entity.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItemEntity, CartItemEntity.CartItemId> {
    
    /**
     * Tìm cart item theo cart ID và product ID
     */
    Optional<CartItemEntity> findByCartIdAndProductId(Integer cartId, String productId);
    
    /**
     * Tìm tất cả cart items theo cart ID
     */
    List<CartItemEntity> findByCartId(Integer cartId);
    
    /**
     * Tìm tất cả cart items theo product ID
     */
    List<CartItemEntity> findByProductId(String productId);
    
    /**
     * Tìm cart items theo user ID
     */
    @Query("SELECT ci FROM cart_items ci JOIN ci.cart c WHERE c.user.userId = :userId")
    List<CartItemEntity> findByUserId(@Param("userId") String userId);
    
    /**
     * Tìm cart item theo user ID và product ID
     */
    @Query("SELECT ci FROM cart_items ci JOIN ci.cart c WHERE c.user.userId = :userId AND ci.productId = :productId")
    Optional<CartItemEntity> findByUserIdAndProductId(@Param("userId") String userId, @Param("productId") String productId);
    
    /**
     * Đếm số lượng cart items theo cart ID
     */
    Long countByCartId(Integer cartId);
    
    /**
     * Đếm số lượng cart items theo user ID
     */
    @Query("SELECT COUNT(ci) FROM cart_items ci JOIN ci.cart c WHERE c.user.userId = :userId")
    Long countByUserId(@Param("userId") String userId);
    
    /**
     * Cập nhật quantity của cart item
     */
    @Modifying
    @Query("UPDATE cart_items ci SET ci.quantity = :quantity WHERE ci.cartId = :cartId AND ci.productId = :productId")
    int updateQuantity(@Param("cartId") Integer cartId, @Param("productId") String productId, @Param("quantity") Integer quantity);
    
    /**
     * Xóa cart item theo cart ID và product ID
     */
    @Modifying
    @Query("DELETE FROM cart_items ci WHERE ci.cartId = :cartId AND ci.productId = :productId")
    int deleteByCartIdAndProductId(@Param("cartId") Integer cartId, @Param("productId") String productId);
    
    /**
     * Xóa tất cả cart items theo cart ID
     */
    @Modifying
    @Query("DELETE FROM cart_items ci WHERE ci.cartId = :cartId")
    int deleteByCartId(@Param("cartId") Integer cartId);
    
    /**
     * Xóa tất cả cart items theo user ID
     */
    @Modifying
    @Query("DELETE FROM cart_items ci WHERE ci.cart.cartId IN (SELECT c.cartId FROM cart c WHERE c.user.userId = :userId)")
    int deleteByUserId(@Param("userId") String userId);
    
    /**
     * Tìm cart items với product details
     */
    @Query("SELECT ci FROM cart_items ci LEFT JOIN FETCH ci.product p LEFT JOIN FETCH p.brand LEFT JOIN FETCH p.category WHERE ci.cartId = :cartId")
    List<CartItemEntity> findCartItemsWithProductDetails(@Param("cartId") Integer cartId);
} 