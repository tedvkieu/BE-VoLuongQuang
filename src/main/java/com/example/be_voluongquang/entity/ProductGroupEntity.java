package com.example.be_voluongquang.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "product_group")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductGroupEntity extends BaseEntity {
    @Id
    @Column(name = "gr_prd_id")
    private String groupId;

    @Column(name = "gr_prd_name", nullable = false)
    private String groupName;

    @OneToMany(mappedBy = "productGroup")
    private List<ProductEntity> products = new ArrayList<>();
} 