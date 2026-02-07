package com.example.be_voluongquang.repository;

import com.example.be_voluongquang.entity.PurchaseOrderItemEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItemEntity, String> {
    List<PurchaseOrderItemEntity> findByPurchaseOrderId(String purchaseOrderId);
}

