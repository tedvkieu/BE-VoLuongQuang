package com.example.be_voluongquang.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "product")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEntity extends BaseEntity {
    @Id
    @Column(name = "product_id")
    private String productId;

    @Column(name = "product_name", nullable = false)
    private String name;

    @Column(name = "price")
    private Double price;

    @Column(name = "cost_price")
    private Double costPrice;

    @Column(name = "wholesale_price")
    private Double wholesalePrice;

    @Column(name = "discount_percent")
    private Integer discountPercent;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "unit")
    private String unit;

    @Column(name = "is_featured")
    private Boolean isFeatured;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "url_shopee")
    private String urlShopee;

    @Column(name = "url_lazada")
    private String urlLazada;

    @Column(name = "url_other")
    private String urlOther;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    private BrandEntity brand;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    @ManyToOne
    @JoinColumn(name = "product_group_id")
    private ProductGroupEntity productGroup;

    @OneToMany(mappedBy = "product")
    @Builder.Default
    private List<FileArchivalEntity> files = new ArrayList<>();

}
