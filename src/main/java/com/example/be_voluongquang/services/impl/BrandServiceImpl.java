package com.example.be_voluongquang.services.impl;

import com.example.be_voluongquang.dto.request.brand.BrandRequestDTO;
import com.example.be_voluongquang.dto.response.BrandSimpleDTO;
import com.example.be_voluongquang.dto.response.PagedResponse;
import com.example.be_voluongquang.dto.response.brand.BrandResponseDTO;
import com.example.be_voluongquang.entity.BrandEntity;
import com.example.be_voluongquang.exception.ResourceNotFoundException;
import com.example.be_voluongquang.repository.BrandRepository;
import com.example.be_voluongquang.services.BrandService;
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
public class BrandServiceImpl implements BrandService {
    private static final String BRAND_LABEL = "Brand";

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

    @Override
    public BrandResponseDTO getBrandById(String id) {
        BrandEntity brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(BRAND_LABEL, "brandId", id));
        return toResponse(brand);
    }

    @Override
    public BrandResponseDTO createBrand(BrandRequestDTO request) {
        String brandName = normalizeName(request.getBrandName(), BRAND_LABEL);
        if (brandRepository.existsByBrandNameIgnoreCase(brandName)) {
            throw new IllegalArgumentException("Brand name already exists");
        }

        String brandId = resolveId(request.getBrandId());
        if (brandRepository.existsById(brandId)) {
            throw new IllegalArgumentException("Brand id already exists");
        }

        BrandEntity brand = BrandEntity.builder()
                .brandId(brandId)
                .brandName(brandName)
                .build();

        return toResponse(brandRepository.save(brand));
    }

    @Override
    @Transactional
    public BrandResponseDTO updateBrand(String id, BrandRequestDTO request) {
        String brandName = normalizeName(request.getBrandName(), BRAND_LABEL);

        BrandEntity brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(BRAND_LABEL, "brandId", id));

        Optional<BrandEntity> existing = brandRepository.findByBrandNameIgnoreCase(brandName);
        if (existing.isPresent() && !existing.get().getBrandId().equals(brand.getBrandId())) {
            throw new IllegalArgumentException("Brand name already exists");
        }

        brand.setBrandName(brandName);
        return toResponse(brandRepository.save(brand));
    }

    @Override
    @Transactional
    public void deleteBrand(String id) {
        BrandEntity brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(BRAND_LABEL, "brandId", id));
        brandRepository.delete(brand);
    }

    @Override
    public PagedResponse<BrandResponseDTO> getBrandsPage(int page, int size, String search) {
        int safePage = Math.max(0, page);
        int safeSize = size <= 0 ? 10 : size;
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<BrandEntity> entityPage;
        if (StringUtils.hasText(search)) {
            entityPage = brandRepository.findByBrandNameContainingIgnoreCase(search.trim(), pageable);
        } else {
            entityPage = brandRepository.findAll(pageable);
        }

        Page<BrandResponseDTO> dtoPage = entityPage.map(this::toResponse);
        return PagedResponse.from(dtoPage);
    }

    private BrandResponseDTO toResponse(BrandEntity entity) {
        return BrandResponseDTO.builder()
                .brandId(entity.getBrandId())
                .brandName(entity.getBrandName())
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
