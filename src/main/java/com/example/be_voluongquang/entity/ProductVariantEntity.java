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

@Entity(name = "product_variant")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "product_variant_id")
    private String productVariantId;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(name = "variant_name", nullable = false)
    private String variantName;

    @Column(name = "variant_price", nullable = false)
    private Double variantPrice;

    @Column(name = "final_price")
    private Double finalPrice;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @Column(name = "sort_order")
    private Integer sortOrder;
}
