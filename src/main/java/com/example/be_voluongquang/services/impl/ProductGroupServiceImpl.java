package com.example.be_voluongquang.services.impl;

import com.example.be_voluongquang.dto.request.productgroup.ProductGroupRequestDTO;
import com.example.be_voluongquang.dto.response.PagedResponse;
import com.example.be_voluongquang.dto.response.ProductGroupSimpleDTO;
import com.example.be_voluongquang.dto.response.productgroup.ProductGroupResponseDTO;
import com.example.be_voluongquang.entity.ProductGroupEntity;
import com.example.be_voluongquang.exception.ResourceNotFoundException;
import com.example.be_voluongquang.repository.ProductGroupRepository;
import com.example.be_voluongquang.services.ProductGroupService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductGroupServiceImpl implements ProductGroupService {
    private static final String PRODUCT_GROUP_LABEL = "Product Group";

    @Autowired
    private ProductGroupRepository productGroupRepository;

    @Override
    public List<ProductGroupSimpleDTO> getAllProductGroups() {
        List<ProductGroupEntity> groups = productGroupRepository.findAll();
        return groups.stream()
                .map(g -> ProductGroupSimpleDTO.builder()
                        .groupId(g.getGroupId())
                        .groupName(g.getGroupName())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public ProductGroupResponseDTO getProductGroupById(String id) {
        ProductGroupEntity group = productGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_GROUP_LABEL, "groupId", id));
        return toResponse(group);
    }

    @Override
    public ProductGroupResponseDTO createProductGroup(ProductGroupRequestDTO request) {
        String groupName = normalizeName(request.getGroupName(), PRODUCT_GROUP_LABEL);
        if (productGroupRepository.existsByGroupNameIgnoreCase(groupName)) {
            throw new IllegalArgumentException("Product group name already exists");
        }

        String groupId = resolveId(request.getGroupId());
        if (productGroupRepository.existsById(groupId)) {
            throw new IllegalArgumentException("Product group id already exists");
        }

        ProductGroupEntity group = ProductGroupEntity.builder()
                .groupId(groupId)
                .groupName(groupName)
                .build();

        return toResponse(productGroupRepository.save(group));
    }

    @Override
    @Transactional
    public ProductGroupResponseDTO updateProductGroup(String id, ProductGroupRequestDTO request) {
        String groupName = normalizeName(request.getGroupName(), PRODUCT_GROUP_LABEL);

        ProductGroupEntity group = productGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_GROUP_LABEL, "groupId", id));

        Optional<ProductGroupEntity> existing = productGroupRepository.findByGroupNameIgnoreCase(groupName);
        if (existing.isPresent() && !existing.get().getGroupId().equals(group.getGroupId())) {
            throw new IllegalArgumentException("Product group name already exists");
        }

        group.setGroupName(groupName);
        return toResponse(productGroupRepository.save(group));
    }

    @Override
    @Transactional
    public void deleteProductGroup(String id) {
        ProductGroupEntity group = productGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_GROUP_LABEL, "groupId", id));
        productGroupRepository.delete(group);
    }

    @Override
    public PagedResponse<ProductGroupResponseDTO> getProductGroupsPage(int page, int size, String search) {
        int safePage = Math.max(0, page);
        int safeSize = size <= 0 ? 10 : size;
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<ProductGroupEntity> entityPage;
        if (StringUtils.hasText(search)) {
            entityPage = productGroupRepository.findByGroupNameContainingIgnoreCase(search.trim(), pageable);
        } else {
            entityPage = productGroupRepository.findAll(pageable);
        }

        Page<ProductGroupResponseDTO> dtoPage = entityPage.map(this::toResponse);
        return PagedResponse.from(dtoPage);
    }

    private ProductGroupResponseDTO toResponse(ProductGroupEntity entity) {
        return ProductGroupResponseDTO.builder()
                .groupId(entity.getGroupId())
                .groupName(entity.getGroupName())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private String normalizeName(String raw, String label) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " name is required");
        }
        return raw.trim();
    }

    private String resolveId(String providedId) {
        String normalizedId = providedId != null ? providedId.trim() : null;
        if (normalizedId == null || normalizedId.isEmpty()) {
            return UUID.randomUUID().toString();
        }
        return normalizedId;
    }
}
