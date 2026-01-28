package com.example.be_voluongquang.services;

import com.example.be_voluongquang.dto.request.category.CategoryRequestDTO;
import com.example.be_voluongquang.dto.response.CategorySimpleDTO;
import com.example.be_voluongquang.dto.response.PagedResponse;
import com.example.be_voluongquang.dto.response.category.CategoryResponseDTO;
import java.util.List;

public interface CategoryService {
    List<CategorySimpleDTO> getAllCategories();

    CategoryResponseDTO getCategoryById(String id);

    CategoryResponseDTO createCategory(CategoryRequestDTO request);

    CategoryResponseDTO updateCategory(String id, CategoryRequestDTO request);

    void deleteCategory(String id);

    PagedResponse<CategoryResponseDTO> getCategoriesPage(int page, int size, String search);
}
