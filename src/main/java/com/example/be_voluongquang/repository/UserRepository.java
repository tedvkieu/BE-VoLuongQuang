package com.example.be_voluongquang.repository;

import com.example.be_voluongquang.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {
    
    /**
     * Tìm user theo email
     */
    Optional<UserEntity> findByEmail(String email);
    
    /**
     * Tìm user theo email (case insensitive)
     */
    Optional<UserEntity> findByEmailIgnoreCase(String email);
    
    /**
     * Kiểm tra email đã tồn tại chưa
     */
    boolean existsByEmail(String email);
    
    /**
     * Kiểm tra email đã tồn tại chưa (case insensitive)
     */
    boolean existsByEmailIgnoreCase(String email);
    
    /**
     * Tìm user theo phone
     */
    Optional<UserEntity> findByPhone(String phone);
    
    /**
     * Kiểm tra phone đã tồn tại chưa
     */
    boolean existsByPhone(String phone);
    
    /**
     * Tìm user theo role
     */
    List<UserEntity> findByRole(String role);
    
    /**
     * Tìm user theo role và active status
     */
    @Query("SELECT u FROM users u WHERE u.role = :role")
    List<UserEntity> findUsersByRole(@Param("role") String role);
    
    /**
     * Tìm user theo tên (chứa)
     */
    List<UserEntity> findByFullNameContainingIgnoreCase(String fullName);
    
    /**
     * Tìm user theo email hoặc tên
     */
    @Query("SELECT u FROM users u WHERE u.email LIKE %:searchTerm% OR u.fullName LIKE %:searchTerm%")
    List<UserEntity> findByEmailOrFullNameContaining(@Param("searchTerm") String searchTerm);
    
    /**
     * Đếm số lượng user theo role
     */
    @Query("SELECT u.role, COUNT(u) FROM users u GROUP BY u.role")
    List<Object[]> countUsersByRole();
    
    /**
     * Tìm user với cart
     */
    @Query("SELECT u FROM users u LEFT JOIN FETCH u.cart WHERE u.userId = :userId")
    Optional<UserEntity> findUserWithCart(@Param("userId") String userId);
    
    /**
     * Tìm user với cart và cart items
     */
    @Query("SELECT u FROM users u LEFT JOIN FETCH u.cart c LEFT JOIN FETCH c.cartItems ci LEFT JOIN FETCH ci.product WHERE u.userId = :userId")
    Optional<UserEntity> findUserWithCartAndItems(@Param("userId") String userId);
}