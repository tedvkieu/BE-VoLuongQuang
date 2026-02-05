package com.example.be_voluongquang.services.impl;

import com.example.be_voluongquang.dto.request.banner.BannerRequestDTO;
import com.example.be_voluongquang.dto.response.PagedResponse;
import com.example.be_voluongquang.dto.response.banner.BannerResponseDTO;
import com.example.be_voluongquang.entity.BannerEntity;
import com.example.be_voluongquang.entity.FileArchivalEntity;
import com.example.be_voluongquang.exception.FileUploadException;
import com.example.be_voluongquang.exception.ResourceNotFoundException;
import com.example.be_voluongquang.repository.BannerRepository;
import com.example.be_voluongquang.repository.FileArchivalRepository;
import com.example.be_voluongquang.services.BannerService;
import com.example.be_voluongquang.services.app.UploadImgImgService;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class BannerServiceImpl implements BannerService {
    private static final String BANNER_LABEL = "Banner";
    private static final String BANNER_UPLOAD_FOLDER = "images/banner";

    @Autowired
    private BannerRepository bannerRepository;

    @Autowired
    private FileArchivalRepository fileArchivalRepository;

    @Autowired
    private UploadImgImgService uploadImgImgService;

    @Override
    public PagedResponse<BannerResponseDTO> getBannersPage(int page, int size, String search, Boolean isDeleted) {
        int safePage = Math.max(0, page);
        int safeSize = size <= 0 ? 10 : size;
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Boolean deletedFilter = isDeleted != null ? isDeleted : Boolean.FALSE;
        Page<BannerEntity> entityPage;
        if (StringUtils.hasText(search)) {
            entityPage = bannerRepository.findByTitleContainingIgnoreCaseAndIsDeleted(search.trim(), deletedFilter, pageable);
        } else {
            entityPage = bannerRepository.findByIsDeleted(deletedFilter, pageable);
        }

        Page<BannerResponseDTO> dtoPage = entityPage.map(this::toResponse);
        return PagedResponse.from(dtoPage);
    }

    @Override
    public BannerResponseDTO getBannerById(String id) {
        BannerEntity banner = bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(BANNER_LABEL, "bannerId", id));
        if (Boolean.TRUE.equals(banner.getIsDeleted())) {
            throw new ResourceNotFoundException(BANNER_LABEL, "bannerId", id);
        }
        return toResponse(banner);
    }

    @Override
    @Transactional
    public BannerResponseDTO createBanner(MultipartFile image, BannerRequestDTO request) {
        if (image == null || image.isEmpty()) {
            throw new FileUploadException("Banner image is required");
        }

        BannerRequestDTO payload = request != null ? request : new BannerRequestDTO();
        String bannerId = resolveId(payload.getBannerId());
        if (bannerRepository.existsById(bannerId)) {
            throw new IllegalArgumentException("Banner id already exists");
        }

        String imageUrl = uploadImgImgService.handleSaveUploadFile(image, BANNER_UPLOAD_FOLDER);
        FileArchivalEntity file = FileArchivalEntity.builder()
                .fileId(UUID.randomUUID().toString())
                .fileUrl(imageUrl)
                .storageProvider("R2")
                .build();
        FileArchivalEntity savedFile = fileArchivalRepository.save(file);

        BannerEntity banner = BannerEntity.builder()
                .bannerId(bannerId)
                .title(normalizeNullable(payload.getTitle()))
                .linkUrl(normalizeNullable(payload.getLinkUrl()))
                .sortOrder(payload.getSortOrder() != null ? payload.getSortOrder() : 0)
                .isActive(payload.getIsActive() != null ? payload.getIsActive() : Boolean.TRUE)
                .image(savedFile)
                .build();

        return toResponse(bannerRepository.save(banner));
    }

    @Override
    @Transactional
    public BannerResponseDTO updateBanner(String id, MultipartFile image, BannerRequestDTO request) {
        BannerEntity banner = bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(BANNER_LABEL, "bannerId", id));

        BannerRequestDTO payload = request != null ? request : new BannerRequestDTO();
        if (payload.getTitle() != null) {
            banner.setTitle(normalizeNullable(payload.getTitle()));
        }
        if (payload.getLinkUrl() != null) {
            banner.setLinkUrl(normalizeNullable(payload.getLinkUrl()));
        }
        if (payload.getSortOrder() != null) {
            banner.setSortOrder(payload.getSortOrder());
        }
        if (payload.getIsActive() != null) {
            banner.setIsActive(payload.getIsActive());
        }

        FileArchivalEntity oldImage = banner.getImage();
        if (image != null && !image.isEmpty()) {
            String imageUrl = uploadImgImgService.handleSaveUploadFile(image, BANNER_UPLOAD_FOLDER);
            FileArchivalEntity newFile = FileArchivalEntity.builder()
                    .fileId(UUID.randomUUID().toString())
                    .fileUrl(imageUrl)
                    .storageProvider("R2")
                    .build();
            FileArchivalEntity savedFile = fileArchivalRepository.save(newFile);
            banner.setImage(savedFile);
        }

        BannerEntity saved = bannerRepository.save(banner);
        if (image != null && !image.isEmpty() && oldImage != null) {
            fileArchivalRepository.deleteById(oldImage.getFileId());
        }
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteBanner(String id) {
        BannerEntity banner = bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(BANNER_LABEL, "bannerId", id));
        banner.setIsDeleted(true);
        bannerRepository.save(banner);
    }

    @Override
    @Transactional
    public BannerResponseDTO restoreBanner(String id) {
        BannerEntity banner = bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(BANNER_LABEL, "bannerId", id));

        if (Boolean.FALSE.equals(banner.getIsDeleted())) {
            return toResponse(banner);
        }

        banner.setIsDeleted(false);
        int updated = bannerRepository.softRestoreById(id);
        if (updated == 0) {
            bannerRepository.save(banner);
        }
        return toResponse(banner);
    }

    @Override
    public List<BannerResponseDTO> getActiveBanners() {
        return bannerRepository.findByIsDeletedFalseAndIsActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private BannerResponseDTO toResponse(BannerEntity entity) {
        FileArchivalEntity image = entity.getImage();
        return BannerResponseDTO.builder()
                .bannerId(entity.getBannerId())
                .title(entity.getTitle())
                .linkUrl(entity.getLinkUrl())
                .sortOrder(entity.getSortOrder())
                .isActive(entity.getIsActive())
                .imageId(image != null ? image.getFileId() : null)
                .imageUrl(image != null ? image.getFileUrl() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .isDeleted(entity.getIsDeleted())
                .build();
    }

    private String normalizeNullable(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String resolveId(String providedId) {
        String normalizedId = providedId != null ? providedId.trim() : null;
        if (normalizedId == null || normalizedId.isEmpty()) {
            return UUID.randomUUID().toString();
        }
        return normalizedId;
    }
}
