package com.example.be_voluongquang.repository;

import com.example.be_voluongquang.entity.BrandEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<BrandEntity, String> {
    
    /**
     * Tìm brand theo tên
     */
    Optional<BrandEntity> findByBrandName(String brandName);
    
    /**
     * Tìm brand theo tên (case insensitive)
     */
    Optional<BrandEntity> findByBrandNameIgnoreCase(String brandName);
    
    /**
     * Tìm tất cả brand có chứa tên
     */
    List<BrandEntity> findByBrandNameContainingIgnoreCase(String brandName);
    
    /**
     * Tìm brand theo tên chính xác
     */
    @Query("SELECT b FROM brand b WHERE b.brandName = :brandName")
    Optional<BrandEntity> findBrandByName(@Param("brandName") String brandName);
    
    /**
     * Đếm số lượng product của mỗi brand
     */
    @Query("SELECT b.brandName, COUNT(p) FROM brand b LEFT JOIN b.products p GROUP BY b.brandName")
    List<Object[]> countProductsByBrand();
    
    /**
     * Tìm brand có nhiều product nhất
     */
    @Query("SELECT b FROM brand b WHERE SIZE(b.products) = (SELECT MAX(SIZE(b2.products)) FROM brand b2)")
    List<BrandEntity> findBrandsWithMostProducts();
} 