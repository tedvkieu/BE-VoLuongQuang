package com.example.be_voluongquang.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Where;
import lombok.*;

import java.io.Serializable;

@Entity(name = "cart_items")
@Where(clause = "is_deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(CartItemEntity.CartItemId.class)
public class CartItemEntity extends BaseEntity {
    @Id
    @Column(name = "cart_id")
    private Integer cartId;

    @Id
    @Column(name = "product_id")
    private String productId;

    @ManyToOne
    @JoinColumn(name = "cart_id", referencedColumnName = "cart_id", insertable = false, updatable = false)
    private CartEntity cart;

    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "product_id", insertable = false, updatable = false)
    private ProductEntity product;

    @Column(name = "quantity")
    private Integer quantity;

    // Composite Key Class
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemId implements Serializable {
        private Integer cartId;
        private String productId;
    }
}
