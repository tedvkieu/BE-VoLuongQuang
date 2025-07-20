package com.example.be_voluongquang.controller;

import com.example.be_voluongquang.dto.response.CategorySimpleDTO;
import com.example.be_voluongquang.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping(path = "/api/category", produces = MediaType.APPLICATION_JSON_VALUE)
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping()
    public List<CategorySimpleDTO> getAllCategories() {
        return categoryService.getAllCategories();
    }
} 