package com.example.be_voluongquang.repository;

import com.example.be_voluongquang.entity.ContactEntity;
import com.example.be_voluongquang.entity.ContactStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepository extends JpaRepository<ContactEntity, String> {
    List<ContactEntity> findByIsDeletedFalseOrderByCreatedAtDesc();

    List<ContactEntity> findByIsDeletedFalseAndStatusOrderByCreatedAtDesc(ContactStatus status);

    @Query("""
            SELECT COUNT(c) FROM contact c
            WHERE (:isDeleted IS NULL OR c.isDeleted = :isDeleted)
              AND (:status IS NULL OR c.status = :status)
            """)
    long countContacts(@Param("status") ContactStatus status, @Param("isDeleted") Boolean isDeleted);
}
