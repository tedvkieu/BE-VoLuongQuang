package com.example.be_voluongquang.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "purchase_order_item")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderItemEntity extends BaseEntity {

    @Id
    @Column(name = "purchase_order_item_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String purchaseOrderItemId;

    @Column(name = "purchase_order_id", nullable = false)
    private String purchaseOrderId;

    @ManyToOne
    @JoinColumn(
            name = "purchase_order_id",
            referencedColumnName = "purchase_order_id",
            insertable = false,
            updatable = false)
    private PurchaseOrderEntity purchaseOrder;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @ManyToOne
    @JoinColumn(
            name = "product_id",
            referencedColumnName = "product_id",
            insertable = false,
            updatable = false)
    private ProductEntity product;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_unit")
    private String productUnit;

    @Column(name = "product_image_url", length = 2048)
    private String productImageUrl;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price")
    private Double unitPrice;

    @Column(name = "discount_percent")
    private Integer discountPercent;

    @Column(name = "final_unit_price")
    private Double finalUnitPrice;

    @Column(name = "line_total")
    private Double lineTotal;

    @Column(name = "final_line_total")
    private Double finalLineTotal;
}

