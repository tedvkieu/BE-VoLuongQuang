package com.example.be_voluongquang.services;

import com.example.be_voluongquang.dto.request.banner.BannerRequestDTO;
import com.example.be_voluongquang.dto.response.PagedResponse;
import com.example.be_voluongquang.dto.response.banner.BannerResponseDTO;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface BannerService {
    PagedResponse<BannerResponseDTO> getBannersPage(int page, int size, String search, Boolean isDeleted);

    BannerResponseDTO getBannerById(String id);

    BannerResponseDTO createBanner(MultipartFile image, BannerRequestDTO request);

    BannerResponseDTO updateBanner(String id, MultipartFile image, BannerRequestDTO request);

    void deleteBanner(String id);

    BannerResponseDTO restoreBanner(String id);

    List<BannerResponseDTO> getActiveBanners();
}
