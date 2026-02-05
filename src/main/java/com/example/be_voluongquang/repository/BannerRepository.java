package com.example.be_voluongquang.repository;

import com.example.be_voluongquang.entity.BannerEntity;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BannerRepository extends JpaRepository<BannerEntity, String> {

    Page<BannerEntity> findByIsDeleted(Boolean isDeleted, Pageable pageable);

    Page<BannerEntity> findByTitleContainingIgnoreCaseAndIsDeleted(String title, Boolean isDeleted, Pageable pageable);

    List<BannerEntity> findByIsDeletedFalseAndIsActiveTrueOrderBySortOrderAsc();

    @Modifying
    @Query(value = "UPDATE banner SET is_deleted = false WHERE banner_id = :bannerId", nativeQuery = true)
    int softRestoreById(@Param("bannerId") String bannerId);
}
