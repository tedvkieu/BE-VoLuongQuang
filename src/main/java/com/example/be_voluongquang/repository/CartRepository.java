package com.example.be_voluongquang.repository;

import com.example.be_voluongquang.entity.CartEntity;
import com.example.be_voluongquang.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<CartEntity, Integer> {
    
    /**
     * Tìm cart theo user
     */
    Optional<CartEntity> findByUser(UserEntity user);
    
    /**
     * Tìm cart theo user ID
     */
    Optional<CartEntity> findByUserUserId(String userId);
    
    /**
     * Kiểm tra user có cart không
     */
    boolean existsByUserUserId(String userId);
    
    /**
     * Tìm cart với cart items
     */
    @Query("""
            SELECT DISTINCT c
            FROM cart c
            LEFT JOIN FETCH c.cartItems ci
            LEFT JOIN FETCH ci.product
            LEFT JOIN FETCH ci.productVariant
            WHERE c.user.userId = :userId
            """)
    Optional<CartEntity> findCartWithItemsByUserId(@Param("userId") String userId);
    
    /**
     * Tìm cart với cart items và product details
     */
    @Query("""
            SELECT c
            FROM cart c
            LEFT JOIN FETCH c.cartItems ci
            LEFT JOIN FETCH ci.product p
            LEFT JOIN FETCH ci.productVariant pv
            LEFT JOIN FETCH p.brand
            LEFT JOIN FETCH p.category
            WHERE c.user.userId = :userId
            """)
    Optional<CartEntity> findCartWithItemsAndProductDetailsByUserId(@Param("userId") String userId);
    
    /**
     * Đếm số lượng item trong cart
     */
    @Query("SELECT COUNT(ci) FROM cart c JOIN c.cartItems ci WHERE c.user.userId = :userId")
    Long countCartItemsByUserId(@Param("userId") String userId);
    
    /**
     * Tính tổng giá trị cart
     */
    @Query("SELECT SUM(ci.quantity * p.price) FROM cart c JOIN c.cartItems ci JOIN ci.product p WHERE c.user.userId = :userId")
    Double calculateCartTotalByUserId(@Param("userId") String userId);
    
    /**
     * Tìm cart có nhiều item nhất
     */
    @Query("SELECT c FROM cart c WHERE SIZE(c.cartItems) = (SELECT MAX(SIZE(c2.cartItems)) FROM cart c2)")
    List<CartEntity> findCartsWithMostItems();
} 
