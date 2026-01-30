package com.example.be_voluongquang.repository;

import com.example.be_voluongquang.entity.ProductGroupEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductGroupRepository extends JpaRepository<ProductGroupEntity, String> {

    @Modifying
    @Query(value = "UPDATE product_group SET is_deleted = false WHERE gr_prd_id = :groupId", nativeQuery = true)
    int softRestoreById(@Param("groupId") String groupId);
    
    /**
     * Tìm product group theo tên
     */
    Optional<ProductGroupEntity> findByGroupName(String groupName);
    
    /**
     * Tìm product group theo tên (case insensitive)
     */
    Optional<ProductGroupEntity> findByGroupNameIgnoreCase(String groupName);

    Optional<ProductGroupEntity> findByGroupNameIgnoreCaseAndIsDeletedFalse(String groupName);

    List<ProductGroupEntity> findByIsDeletedFalse();
    
    /**
     * Tìm tất cả product group có chứa tên
     */
    List<ProductGroupEntity> findByGroupNameContainingIgnoreCase(String groupName);

    /**
     * Tìm nhóm theo tên với phân trang
     */
    Page<ProductGroupEntity> findByGroupNameContainingIgnoreCase(String groupName, Pageable pageable);

    Page<ProductGroupEntity> findByGroupNameContainingIgnoreCaseAndIsDeleted(String groupName, Boolean isDeleted, Pageable pageable);

    Page<ProductGroupEntity> findByIsDeleted(Boolean isDeleted, Pageable pageable);
    /**
     * Kiểm tra tồn tại product group theo tên (case insensitive)
     */
    boolean existsByGroupNameIgnoreCase(String groupName);
    
    /**
     * Tìm product group theo tên chính xác
     */
    @Query("SELECT pg FROM product_group pg WHERE pg.groupName = :groupName")
    Optional<ProductGroupEntity> findProductGroupByName(@Param("groupName") String groupName);
    
    /**
     * Đếm số lượng product của mỗi product group
     */
    @Query("SELECT pg.groupName, COUNT(p) FROM product_group pg LEFT JOIN pg.products p GROUP BY pg.groupName")
    List<Object[]> countProductsByGroup();
    
    /**
     * Tìm product group có nhiều product nhất
     */
    @Query("SELECT pg FROM product_group pg WHERE SIZE(pg.products) = (SELECT MAX(SIZE(pg2.products)) FROM product_group pg2)")
    List<ProductGroupEntity> findProductGroupsWithMostProducts();
    
    /**
     * Tìm product group có product active
     */
    @Query("SELECT DISTINCT pg FROM product_group pg JOIN pg.products p WHERE p.isActive = true")
    List<ProductGroupEntity> findProductGroupsWithActiveProducts();
}
