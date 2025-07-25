package com.example.be_voluongquang.services;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.example.be_voluongquang.dto.request.product.ImportErrorDTO;
import com.example.be_voluongquang.dto.request.product.ProductRequestDTO;
import com.example.be_voluongquang.dto.response.product.ProductResponseDTO;

public interface ProductService {

    // Service for GET method ----------------------------------
    List<ProductResponseDTO> getFeaturedProducts();

    List<ProductResponseDTO> getAllProductsDiscount();

    List<ProductResponseDTO> getAllProduct();

    // Service for POST method ----------------------------------
    ProductResponseDTO createAProduct(MultipartFile[] images, ProductRequestDTO product);

    Map<String, Object> importProductsFromCsv(MultipartFile file);

    // Service for PUT method ----------------------------------
    ProductResponseDTO updateAProduct(String id, MultipartFile[] images, ProductRequestDTO product);

    // Service for DELETE method ----------------------------------
    ProductResponseDTO deleteAProduct(String id);

    public void deleteMultipleProducts(List<String> ids);
}