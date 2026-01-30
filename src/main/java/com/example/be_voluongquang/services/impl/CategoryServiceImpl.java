package com.example.be_voluongquang.services.impl;

import com.example.be_voluongquang.dto.request.category.CategoryRequestDTO;
import com.example.be_voluongquang.dto.response.CategorySimpleDTO;
import com.example.be_voluongquang.dto.response.PagedResponse;
import com.example.be_voluongquang.dto.response.category.CategoryResponseDTO;
import com.example.be_voluongquang.entity.CategoryEntity;
import com.example.be_voluongquang.exception.ResourceNotFoundException;
import com.example.be_voluongquang.repository.CategoryRepository;
import com.example.be_voluongquang.services.CategoryService;
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
public class CategoryServiceImpl implements CategoryService {
    private static final String CATEGORY_LABEL = "Category";

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<CategorySimpleDTO> getAllCategories() {
        List<CategoryEntity> categories = categoryRepository.findByIsDeletedFalse();
        return categories.stream()
                .map(c -> CategorySimpleDTO.builder()
                        .categoryId(c.getCategoryId())
                        .categoryName(c.getCategoryName())
                        .categoryCode(c.getCategoryCode())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponseDTO getCategoryById(String id) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_LABEL, "categoryId", id));
        return toResponse(category);
    }

    @Override
    public CategoryResponseDTO createCategory(CategoryRequestDTO request) {
        String categoryName = normalizeName(request.getCategoryName(), CATEGORY_LABEL);
        String categoryCode = normalizeCode(request.getCategoryCode());
        if (categoryRepository.findByCategoryNameIgnoreCaseAndIsDeletedFalse(categoryName).isPresent()) {
            throw new IllegalArgumentException("Category name already exists");
        }

        String categoryId = resolveId(request.getCategoryId());
        if (categoryRepository.existsById(categoryId)) {
            throw new IllegalArgumentException("Category id already exists");
        }

        CategoryEntity category = CategoryEntity.builder()
                .categoryId(categoryId)
                .categoryName(categoryName)
                .categoryCode(categoryCode)
                .build();

        return toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponseDTO updateCategory(String id, CategoryRequestDTO request) {
        String categoryName = normalizeName(request.getCategoryName(), CATEGORY_LABEL);
        String categoryCode = normalizeCode(request.getCategoryCode());

        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_LABEL, "categoryId", id));

        Optional<CategoryEntity> existing = categoryRepository.findByCategoryNameIgnoreCaseAndIsDeletedFalse(categoryName);
        if (existing.isPresent() && !existing.get().getCategoryId().equals(category.getCategoryId())) {
            throw new IllegalArgumentException("Category name already exists");
        }

        category.setCategoryName(categoryName);
        category.setCategoryCode(categoryCode);
        return toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(String id) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_LABEL, "categoryId", id));
        category.setIsDeleted(true);
        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public CategoryResponseDTO restoreCategory(String id) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_LABEL, "categoryId", id));
        
        if (Boolean.FALSE.equals(category.getIsDeleted())) {
            return toResponse(category);
        }

        category.setIsDeleted(false);
        int updated = categoryRepository.softRestoreById(id);
        if (updated == 0) {
            category.setIsDeleted(false);
            categoryRepository.save(category);
        }
        
        return toResponse(category);
    }

    @Override
    public PagedResponse<CategoryResponseDTO> getCategoriesPage(int page, int size, String search, Boolean isDeleted) {
        int safePage = Math.max(0, page);
        int safeSize = size <= 0 ? 10 : size;
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Boolean deletedFilter = isDeleted != null ? isDeleted : Boolean.FALSE;
        Page<CategoryEntity> entityPage;
        if (StringUtils.hasText(search)) {
            entityPage = categoryRepository.findByCategoryNameContainingIgnoreCaseAndIsDeleted(
                    search.trim(), deletedFilter, pageable);
        } else {
            entityPage = categoryRepository.findByIsDeleted(deletedFilter, pageable);
        }

        Page<CategoryResponseDTO> dtoPage = entityPage.map(this::toResponse);
        return PagedResponse.from(dtoPage);
    }

    private CategoryResponseDTO toResponse(CategoryEntity entity) {
        return CategoryResponseDTO.builder()
                .categoryId(entity.getCategoryId())
                .categoryName(entity.getCategoryName())
                .categoryCode(entity.getCategoryCode())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .isDeleted(entity.getIsDeleted())
                .build();
    }

    private String normalizeName(String raw, String label) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " name is required");
        }
        return raw.trim();
    }

    private String normalizeCode(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Category code is required");
        }
        String normalized = raw.trim();
        if (!normalized.matches("^[a-z0-9_]+$")) {
            throw new IllegalArgumentException("Category code must be lowercase and contain only letters, numbers, or underscore");
        }
        return normalized;
    }

    private String resolveId(String providedId) {
        String normalizedId = providedId != null ? providedId.trim() : null;
        if (normalizedId == null || normalizedId.isEmpty()) {
            return UUID.randomUUID().toString();
        }
        return normalizedId;
    }
}
