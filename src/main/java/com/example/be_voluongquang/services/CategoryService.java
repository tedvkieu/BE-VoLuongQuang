package com.example.be_voluongquang.services;

import com.example.be_voluongquang.dto.response.CategorySimpleDTO;
import java.util.List;

public interface CategoryService {
    List<CategorySimpleDTO> getAllCategories();
} 