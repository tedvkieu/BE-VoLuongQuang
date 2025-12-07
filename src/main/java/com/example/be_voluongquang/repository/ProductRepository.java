package com.example.be_voluongquang.repository;

import com.example.be_voluongquang.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, String>, JpaSpecificationExecutor<ProductEntity> {
    
    /**
     * Tìm product theo tên
     */
    Optional<ProductEntity> findByName(String name);
    
    /**
     * Tìm product theo tên (case insensitive)
     */
    Optional<ProductEntity> findByNameIgnoreCase(String name);
    
    /**
     * Tìm product theo tên (chứa)
     */
    List<ProductEntity> findByNameContainingIgnoreCase(String name);
    
    /**
     * Tìm product theo brand
     */
    List<ProductEntity> findByBrandBrandId(String brandId);
    
    /**
     * Tìm product theo category
     */
    List<ProductEntity> findByCategoryCategoryId(String categoryId);
    
    /**
     * Tìm product theo product group
     */
    List<ProductEntity> findByProductGroupGroupId(String groupId);
    
    /**
     * Tìm product theo trạng thái active
     */
    List<ProductEntity> findByIsActive(Boolean isActive);
    
    /**
     * Tìm product theo trạng thái featured
     */
    List<ProductEntity> findByIsFeatured(Boolean isFeatured);
    
    /**
     * Tìm product có discount
     */
    List<ProductEntity> findByDiscountPercentGreaterThan(Integer discountPercent);
    
    /**
     * Tìm product theo khoảng giá
     */
    List<ProductEntity> findByPriceBetween(Double minPrice, Double maxPrice);
    
    /**
     * Tìm product theo giá tối thiểu
     */
    List<ProductEntity> findByPriceGreaterThanEqual(Double minPrice);
    
    /**
     * Tìm product theo giá tối đa
     */
    List<ProductEntity> findByPriceLessThanEqual(Double maxPrice);
    
    /**
     * Tìm product theo stock quantity
     */
    List<ProductEntity> findByStockQuantityGreaterThan(Integer quantity);
    
    /**
     * Tìm product hết hàng
     */
    List<ProductEntity> findByStockQuantityLessThanEqual(Integer quantity);
    
    /**
     * Tìm product theo brand và active
     */
    List<ProductEntity> findByBrandBrandIdAndIsActive(String brandId, Boolean isActive);
    
    /**
     * Tìm product theo category và active
     */
    List<ProductEntity> findByCategoryCategoryIdAndIsActive(String categoryId, Boolean isActive);
    
    /**
     * Tìm product theo product group và active
     */
    List<ProductEntity> findByProductGroupGroupIdAndIsActive(String groupId, Boolean isActive);
    
    /**
     * Tìm product với brand và category details
     */
    @Query("SELECT p FROM product p LEFT JOIN FETCH p.brand LEFT JOIN FETCH p.category LEFT JOIN FETCH p.productGroup WHERE p.productId = :productId")
    Optional<ProductEntity> findProductWithDetails(@Param("productId") String productId);
    
    /**
     * Tìm tất cả product với brand và category details
     */
    @Query("SELECT p FROM product p LEFT JOIN FETCH p.brand LEFT JOIN FETCH p.category LEFT JOIN FETCH p.productGroup")
    List<ProductEntity> findAllProductsWithDetails();
    
    /**
     * Tìm product theo tên với pagination
     */
    Page<ProductEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    /**
     * Tìm product theo brand với pagination
     */
    Page<ProductEntity> findByBrandBrandId(String brandId, Pageable pageable);
    
    /**
     * Tìm product theo category với pagination
     */
    Page<ProductEntity> findByCategoryCategoryId(String categoryId, Pageable pageable);
    
    /**
     * Tìm product theo product group với pagination
     */
    Page<ProductEntity> findByProductGroupGroupId(String groupId, Pageable pageable);
    
    /**
     * Tìm product theo trạng thái active với pagination
     */
    Page<ProductEntity> findByIsActive(Boolean isActive, Pageable pageable);
    
    /**
     * Tìm product theo trạng thái featured với pagination
     */
    Page<ProductEntity> findByIsFeatured(Boolean isFeatured, Pageable pageable);
    
    /**
     * Tìm product có discount với pagination
     */
    Page<ProductEntity> findByDiscountPercentGreaterThan(Integer discountPercent, Pageable pageable);
    
    /**
     * Tìm product theo khoảng giá với pagination
     */
    Page<ProductEntity> findByPriceBetween(Double minPrice, Double maxPrice, Pageable pageable);
    
    /**
     * Tìm product theo tên hoặc mô tả
     */
    @Query("SELECT p FROM product p WHERE p.name LIKE %:searchTerm% OR p.description LIKE %:searchTerm%")
    List<ProductEntity> findByNameOrDescriptionContaining(@Param("searchTerm") String searchTerm);
    
    /**
     * Tìm product theo tên hoặc mô tả với pagination
     */
    @Query("SELECT p FROM product p WHERE p.name LIKE %:searchTerm% OR p.description LIKE %:searchTerm%")
    Page<ProductEntity> findByNameOrDescriptionContaining(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Tìm product có discount với pagination và hỗ trợ tìm kiếm theo id hoặc tên
     */
    @Query("SELECT p FROM product p " +
            "WHERE p.discountPercent > :minDiscount " +
            "AND ( :searchTerm IS NULL " +
            "       OR LOWER(p.productId) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "       OR LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "       OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) )")
    Page<ProductEntity> searchDiscountedProducts(@Param("minDiscount") Integer minDiscount,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);
    
    /**
     * Đếm số lượng product theo brand
     */
    @Query("SELECT p.brand.brandName, COUNT(p) FROM product p GROUP BY p.brand.brandName")
    List<Object[]> countProductsByBrand();
    
    /**
     * Đếm số lượng product theo category
     */
    @Query("SELECT p.category.categoryName, COUNT(p) FROM product p GROUP BY p.category.categoryName")
    List<Object[]> countProductsByCategory();
    
    /**
     * Đếm số lượng product theo product group
     */
    @Query("SELECT p.productGroup.groupName, COUNT(p) FROM product p GROUP BY p.productGroup.groupName")
    List<Object[]> countProductsByGroup();
    
    /**
     * Tìm product có giá cao nhất
     */
    @Query("SELECT p FROM product p WHERE p.price = (SELECT MAX(p2.price) FROM product p2)")
    List<ProductEntity> findProductsWithHighestPrice();
    
    /**
     * Tìm product có giá thấp nhất
     */
    @Query("SELECT p FROM product p WHERE p.price = (SELECT MIN(p2.price) FROM product p2)")
    List<ProductEntity> findProductsWithLowestPrice();
    
    /**
     * Tìm product có discount cao nhất
     */
    @Query("SELECT p FROM product p WHERE p.discountPercent = (SELECT MAX(p2.discountPercent) FROM product p2)")
    List<ProductEntity> findProductsWithHighestDiscount();
    
    /**
     * Tìm product theo brand và category
     */
    @Query("SELECT p FROM product p WHERE p.brand.brandId = :brandId AND p.category.categoryId = :categoryId")
    List<ProductEntity> findByBrandAndCategory(@Param("brandId") String brandId, @Param("categoryId") String categoryId);
    
    /**
     * Tìm product theo brand, category và active
     */
    @Query("SELECT p FROM product p WHERE p.brand.brandId = :brandId AND p.category.categoryId = :categoryId AND p.isActive = :isActive")
    List<ProductEntity> findByBrandAndCategoryAndActive(@Param("brandId") String brandId, @Param("categoryId") String categoryId, @Param("isActive") Boolean isActive);
    
    // Existing methods
    List<ProductEntity> findAllByIsFeatured(boolean isFeatured);
    List<ProductEntity> findTop4ByOrderByDiscountPercentDesc();
}
