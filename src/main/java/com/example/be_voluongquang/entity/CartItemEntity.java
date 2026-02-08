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
import org.hibernate.annotations.Where;

@Entity(name = "cart_items")
@Where(clause = "is_deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Long cartItemId;

    @Column(name = "cart_id", nullable = false)
    private Integer cartId;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "product_variant_id")
    private String productVariantId;

    @ManyToOne
    @JoinColumn(
            name = "cart_id",
            referencedColumnName = "cart_id",
            insertable = false,
            updatable = false)
    private CartEntity cart;

    @ManyToOne
    @JoinColumn(
            name = "product_id",
            referencedColumnName = "product_id",
            insertable = false,
            updatable = false)
    private ProductEntity product;

    @ManyToOne
    @JoinColumn(
            name = "product_variant_id",
            referencedColumnName = "product_variant_id",
            insertable = false,
            updatable = false)
    private ProductVariantEntity productVariant;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;
}
