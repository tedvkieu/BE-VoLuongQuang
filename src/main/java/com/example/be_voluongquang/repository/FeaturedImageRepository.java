package com.example.be_voluongquang.repository;

import com.example.be_voluongquang.entity.FeaturedImageEntity;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FeaturedImageRepository extends JpaRepository<FeaturedImageEntity, String> {
    Page<FeaturedImageEntity> findByIsDeleted(Boolean isDeleted, Pageable pageable);

    Page<FeaturedImageEntity> findByTitleContainingIgnoreCaseAndIsDeleted(String title, Boolean isDeleted,
            Pageable pageable);

    List<FeaturedImageEntity> findByIsDeletedFalseAndIsActiveTrueOrderBySortOrderAsc();

    @Modifying
    @Query(value = "UPDATE featured_image SET is_deleted = false WHERE featured_image_id = :id", nativeQuery = true)
    int softRestoreById(@Param("id") String id);
}
