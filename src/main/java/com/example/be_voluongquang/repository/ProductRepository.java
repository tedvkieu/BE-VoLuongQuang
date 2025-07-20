package com.example.be_voluongquang.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.be_voluongquang.entity.ProductEntity;

public interface ProductRepository extends JpaRepository<ProductEntity, String> {
    
    List<ProductEntity> findAllByIsFeatured(boolean isFeatured);

    List<ProductEntity> findTop4ByOrderByDiscountPercentDesc();

}
