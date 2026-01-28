package com.example.be_voluongquang.repository;

import com.example.be_voluongquang.entity.CategoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, String> {
    
    /**
     * Tìm category theo tên
     */
    Optional<CategoryEntity> findByCategoryName(String categoryName);
    
    /**
     * Tìm category theo tên (case insensitive)
     */
    Optional<CategoryEntity> findByCategoryNameIgnoreCase(String categoryName);
    
    /**
     * Tìm tất cả category có chứa tên
     */
    List<CategoryEntity> findByCategoryNameContainingIgnoreCase(String categoryName);

    /**
     * Tìm category theo tên với phân trang
     */
    Page<CategoryEntity> findByCategoryNameContainingIgnoreCase(String categoryName, Pageable pageable);
    /**
     * Kiểm tra tồn tại category theo tên (case insensitive)
     */
    boolean existsByCategoryNameIgnoreCase(String categoryName);
    
    /**
     * Tìm category theo tên chính xác
     */
    @Query("SELECT c FROM category c WHERE c.categoryName = :categoryName")
    Optional<CategoryEntity> findCategoryByName(@Param("categoryName") String categoryName);
    
    /**
     * Đếm số lượng product của mỗi category
     */
    @Query("SELECT c.categoryName, COUNT(p) FROM category c LEFT JOIN c.products p GROUP BY c.categoryName")
    List<Object[]> countProductsByCategory();
    
    /**
     * Tìm category có nhiều product nhất
     */
    @Query("SELECT c FROM category c WHERE SIZE(c.products) = (SELECT MAX(SIZE(c2.products)) FROM category c2)")
    List<CategoryEntity> findCategoriesWithMostProducts();
    
    /**
     * Tìm category có product active
     */
    @Query("SELECT DISTINCT c FROM category c JOIN c.products p WHERE p.isActive = true")
    List<CategoryEntity> findCategoriesWithActiveProducts();
}
