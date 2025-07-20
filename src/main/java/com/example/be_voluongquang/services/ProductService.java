package com.example.be_voluongquang.services;

import java.util.List;

import com.example.be_voluongquang.dto.response.ProductResponseDTO;

public interface ProductService {
    List<ProductResponseDTO> getFeaturedProducts();
    List<ProductResponseDTO> getAllProductsDiscount();
    List<ProductResponseDTO> getAllProduct();
} 