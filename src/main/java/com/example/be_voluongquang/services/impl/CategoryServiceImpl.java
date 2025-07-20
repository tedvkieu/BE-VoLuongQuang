package com.example.be_voluongquang.services.impl;

import com.example.be_voluongquang.dto.response.CategorySimpleDTO;
import com.example.be_voluongquang.entity.CategoryEntity;
import com.example.be_voluongquang.repository.CategoryRepository;
import com.example.be_voluongquang.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {
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
} 