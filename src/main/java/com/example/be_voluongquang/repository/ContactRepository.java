package com.example.be_voluongquang.repository;

import com.example.be_voluongquang.entity.ContactEntity;
import com.example.be_voluongquang.entity.ContactStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepository extends JpaRepository<ContactEntity, String> {
    List<ContactEntity> findByIsDeletedFalseOrderByCreatedAtDesc();

    List<ContactEntity> findByIsDeletedFalseAndStatusOrderByCreatedAtDesc(ContactStatus status);
}
