package com.example.be_voluongquang.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "featured_image")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeaturedImageEntity extends BaseEntity {

    @Id
    @Column(name = "featured_image_id")
    private String featuredImageId;

    @Column(name = "title")
    private String title;

    @Column(name = "link_url", length = 2048)
    private String linkUrl;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "is_active")
    private Boolean isActive;

    @OneToOne
    @JoinColumn(name = "image_id")
    private FileArchivalEntity image;
}

