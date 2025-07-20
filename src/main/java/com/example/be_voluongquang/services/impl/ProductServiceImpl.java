package com.example.be_voluongquang.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.be_voluongquang.dto.response.ProductResponseDTO;
import com.example.be_voluongquang.mapper.ProductMapper;
import com.example.be_voluongquang.repository.ProductRepository;
import com.example.be_voluongquang.services.ProductService;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductMapper productMapper;


    @Override
    public List<ProductResponseDTO> getAllProduct(){
        return productMapper.toDtoList(productRepository.findAll());
    }

    @Override
    public List<ProductResponseDTO> getFeaturedProducts() {
        return productMapper.toDtoList(productRepository.findAllByIsFeatured(true));
    }

    @Override
    public List<ProductResponseDTO> getAllProductsDiscount() {
        return productMapper.toDtoList(productRepository.findTop4ByOrderByDiscountPercentDesc());
    }
}
