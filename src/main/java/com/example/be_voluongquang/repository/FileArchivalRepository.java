package com.example.be_voluongquang.repository;

import com.example.be_voluongquang.entity.FileArchivalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileArchivalRepository extends JpaRepository<FileArchivalEntity, String> {
    void deleteByProduct_ProductId(String productId);
}
