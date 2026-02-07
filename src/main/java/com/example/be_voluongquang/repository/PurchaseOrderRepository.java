package com.example.be_voluongquang.repository;

import com.example.be_voluongquang.entity.PurchaseOrderEntity;
import com.example.be_voluongquang.entity.PurchaseOrderStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrderEntity, String> {

    @Query("SELECT po FROM purchase_order po LEFT JOIN FETCH po.items WHERE po.purchaseOrderId = :id")
    Optional<PurchaseOrderEntity> findByIdWithItems(@Param("id") String id);

    @Query("""
            SELECT po FROM purchase_order po
            WHERE (:isDeleted IS NULL OR po.isDeleted = :isDeleted)
              AND (:status IS NULL OR po.status = :status)
              AND (
                :searchPattern IS NULL
                OR po.customerName LIKE CAST(:searchPattern AS string)
                OR po.customerPhone LIKE CAST(:searchPattern AS string)
                OR COALESCE(po.customerEmail, '') LIKE CAST(:searchPattern AS string)
              )
            """)
    Page<PurchaseOrderEntity> searchOrders(
            @Param("searchPattern") String searchPattern,
            @Param("status") PurchaseOrderStatus status,
            @Param("isDeleted") Boolean isDeleted,
            Pageable pageable);
}
