package com.example.be_voluongquang.mapper;

import com.example.be_voluongquang.dto.request.product.ProductRequestDTO;
import com.example.be_voluongquang.dto.response.product.ProductResponseDTO;
import com.example.be_voluongquang.dto.response.product.ProductVariantResponseDTO;
import com.example.be_voluongquang.entity.BrandEntity;
import com.example.be_voluongquang.entity.CategoryEntity;
import com.example.be_voluongquang.entity.ProductEntity;
import com.example.be_voluongquang.entity.ProductGroupEntity;
import com.example.be_voluongquang.entity.ProductVariantEntity;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper extends BaseMapper<ProductEntity, ProductResponseDTO> {
    public ProductMapper() {
        super(ProductEntity.class, ProductResponseDTO.class);
    }

    public ProductResponseDTO toDTO(ProductEntity entity) {
        if (entity == null) return null;

        List<ProductVariantResponseDTO> productVariants = null;
        if (Hibernate.isInitialized(entity.getProductVariants()) && entity.getProductVariants() != null) {
            productVariants = entity.getProductVariants().stream()
                    .filter(v -> v != null && (v.getIsDeleted() == null || !v.getIsDeleted()))
                    .sorted(Comparator.comparing((ProductVariantEntity v) -> v.getSortOrder() == null ? Integer.MAX_VALUE : v.getSortOrder()))
                    .map(v -> ProductVariantResponseDTO.builder()
                            .productVariantId(v.getProductVariantId())
                            .variantName(v.getVariantName())
                            .variantPrice(v.getVariantPrice())
                            .finalPrice(v.getFinalPrice())
                            .stockQuantity(v.getStockQuantity())
                            .sortOrder(v.getSortOrder())
                            .isDeleted(v.getIsDeleted())
                            .build())
                    .collect(Collectors.toList());
        }

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
                .urlShopee(entity.getUrlShopee())
                .urlLazada(entity.getUrlLazada())
                .urlOther(entity.getUrlOther())
                .description(entity.getDescription())
                .brandId(entity.getBrand() != null ? entity.getBrand().getBrandId() : null)
                .categoryId(entity.getCategory() != null ? entity.getCategory().getCategoryId() : null)
                .productGroupId(entity.getProductGroup() != null ? entity.getProductGroup().getGroupId() : null)
                .isDeleted(entity.getIsDeleted())
                .productVariants(productVariants)
                .build();
    }

    public static ProductEntity toEntity(ProductRequestDTO dto,
                                         BrandEntity brand,
                                         CategoryEntity category,
                                         ProductGroupEntity productGroup,
                                         String imageUrl) {

        return ProductEntity.builder()
                .productId(dto.getProductId())
                .name(dto.getName())
                .price(dto.getPrice())
                .costPrice(dto.getCostPrice())
                .wholesalePrice(dto.getWholesalePrice())
                .discountPercent(dto.getDiscountPercent())
                .stockQuantity(dto.getStockQuantity())
                .weight(dto.getWeight())
                .unit(dto.getUnit())
                .isFeatured(dto.getIsFeatured())
                .isActive(dto.getIsActive())
                .description(dto.getDescription())
                .imageUrl(imageUrl)
                .urlShopee(dto.getUrlShopee())
                .urlLazada(dto.getUrlLazada())
                .urlOther(dto.getUrlOther())
                .brand(brand)
                .category(category)
                .productGroup(productGroup)
                .build();
    }
}
