package com.example.be_voluongquang.repository;

import com.example.be_voluongquang.entity.ProductVariantEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariantEntity, String> {
    List<ProductVariantEntity> findByProductProductIdAndIsDeletedFalseOrderBySortOrderAsc(String productId);

    void deleteByProductProductId(String productId);

    @Modifying
    @Query(value = """
            UPDATE product_variant
            SET final_price = variant_price * (1 - (CAST(:discountPercent AS numeric) / 100))
            WHERE product_id = :productId
            """, nativeQuery = true)
    int recomputeFinalPriceForProduct(@Param("productId") String productId, @Param("discountPercent") int discountPercent);
}
