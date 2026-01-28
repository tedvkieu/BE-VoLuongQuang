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
        List<CategoryEntity> categories = categoryRepository.findAll();
        return categories.stream()
                .map(c -> CategorySimpleDTO.builder()
                        .categoryId(c.getCategoryId())
                        .categoryName(c.getCategoryName())
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
        if (categoryRepository.existsByCategoryNameIgnoreCase(categoryName)) {
            throw new IllegalArgumentException("Category name already exists");
        }

        String categoryId = resolveId(request.getCategoryId());
        if (categoryRepository.existsById(categoryId)) {
            throw new IllegalArgumentException("Category id already exists");
        }

        CategoryEntity category = CategoryEntity.builder()
                .categoryId(categoryId)
                .categoryName(categoryName)
                .build();

        return toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponseDTO updateCategory(String id, CategoryRequestDTO request) {
        String categoryName = normalizeName(request.getCategoryName(), CATEGORY_LABEL);

        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_LABEL, "categoryId", id));

        Optional<CategoryEntity> existing = categoryRepository.findByCategoryNameIgnoreCase(categoryName);
        if (existing.isPresent() && !existing.get().getCategoryId().equals(category.getCategoryId())) {
            throw new IllegalArgumentException("Category name already exists");
        }

        category.setCategoryName(categoryName);
        return toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(String id) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_LABEL, "categoryId", id));
        categoryRepository.delete(category);
    }

    @Override
    public PagedResponse<CategoryResponseDTO> getCategoriesPage(int page, int size, String search) {
        int safePage = Math.max(0, page);
        int safeSize = size <= 0 ? 10 : size;
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<CategoryEntity> entityPage;
        if (StringUtils.hasText(search)) {
            entityPage = categoryRepository.findByCategoryNameContainingIgnoreCase(search.trim(), pageable);
        } else {
            entityPage = categoryRepository.findAll(pageable);
        }

        Page<CategoryResponseDTO> dtoPage = entityPage.map(this::toResponse);
        return PagedResponse.from(dtoPage);
    }

    private CategoryResponseDTO toResponse(CategoryEntity entity) {
        return CategoryResponseDTO.builder()
                .categoryId(entity.getCategoryId())
                .categoryName(entity.getCategoryName())
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
