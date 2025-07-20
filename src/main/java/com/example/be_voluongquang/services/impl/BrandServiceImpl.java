package com.example.be_voluongquang.services.impl;

import com.example.be_voluongquang.dto.response.BrandSimpleDTO;
import com.example.be_voluongquang.entity.BrandEntity;
import com.example.be_voluongquang.repository.BrandRepository;
import com.example.be_voluongquang.services.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BrandServiceImpl implements BrandService {
    @Autowired
    private BrandRepository brandRepository;

    @Override
    public List<BrandSimpleDTO> getAllBrands() {
        List<BrandEntity> brands = brandRepository.findAll();
        return brands.stream()
                .map(b -> BrandSimpleDTO.builder()
                        .brandId(b.getBrandId())
                        .brandName(b.getBrandName())
                        .build())
                .collect(Collectors.toList());
    }
} 