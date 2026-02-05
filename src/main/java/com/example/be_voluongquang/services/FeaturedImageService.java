package com.example.be_voluongquang.services;

import com.example.be_voluongquang.dto.request.featuredimage.FeaturedImageRequestDTO;
import com.example.be_voluongquang.dto.response.PagedResponse;
import com.example.be_voluongquang.dto.response.featuredimage.FeaturedImageResponseDTO;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface FeaturedImageService {
    PagedResponse<FeaturedImageResponseDTO> getFeaturedImagesPage(int page, int size, String search, Boolean isDeleted);

    FeaturedImageResponseDTO getFeaturedImageById(String id);

    FeaturedImageResponseDTO createFeaturedImage(MultipartFile image, FeaturedImageRequestDTO request);

    void deleteFeaturedImage(String id);

    FeaturedImageResponseDTO restoreFeaturedImage(String id);

    List<FeaturedImageResponseDTO> getActiveFeaturedImages();
}

