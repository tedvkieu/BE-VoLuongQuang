package com.example.be_voluongquang.mapper;

import com.example.be_voluongquang.dto.response.ProductResponseDTO;
import com.example.be_voluongquang.entity.ProductEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper extends BaseMapper<ProductEntity, ProductResponseDTO> {
    public ProductMapper() {
        super(ProductEntity.class, ProductResponseDTO.class);
    }

    public ProductResponseDTO toDTO(ProductEntity entity) {
        if (entity == null) return null;
        return ProductResponseDTO.builder()
                .productId(entity.getProductId())
                .name(entity.getName())
                .price(entity.getPrice())
                .costPrice(entity.getCostPrice())
                .wholesalePrice(entity.getWholesalePrice())
                .discountPercent(entity.getDiscountPercent())
                .stockQuantity(entity.getStockQuantity())
                .weight(entity.getWeight())
                .unit(entity.getUnit())
                .isFeatured(entity.getIsFeatured())
                .isActive(entity.getIsActive())
                .imageUrl(entity.getImageUrl())
                .description(entity.getDescription())
                .brandId(entity.getBrand() != null ? entity.getBrand().getBrandId() : null)
                .categoryId(entity.getCategory() != null ? entity.getCategory().getCategoryId() : null)
                .productGroupId(entity.getProductGroup() != null ? entity.getProductGroup().getGroupId() : null)
                .build();
    }
}
