package com.example.be_voluongquang.services;

import com.example.be_voluongquang.dto.request.brand.BrandRequestDTO;
import com.example.be_voluongquang.dto.response.PagedResponse;
import com.example.be_voluongquang.dto.response.brand.BrandResponseDTO;
import com.example.be_voluongquang.dto.response.BrandSimpleDTO;
import java.util.List;

public interface BrandService {
    List<BrandSimpleDTO> getAllBrands();

    BrandResponseDTO getBrandById(String id);

    BrandResponseDTO createBrand(BrandRequestDTO request);

    BrandResponseDTO updateBrand(String id, BrandRequestDTO request);

    void deleteBrand(String id);

    PagedResponse<BrandResponseDTO> getBrandsPage(int page, int size, String search, Boolean isDeleted);
}
