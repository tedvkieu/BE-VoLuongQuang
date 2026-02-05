package com.example.be_voluongquang.services.impl;

import com.example.be_voluongquang.dto.request.featuredimage.FeaturedImageRequestDTO;
import com.example.be_voluongquang.dto.response.PagedResponse;
import com.example.be_voluongquang.dto.response.featuredimage.FeaturedImageResponseDTO;
import com.example.be_voluongquang.entity.FeaturedImageEntity;
import com.example.be_voluongquang.entity.FileArchivalEntity;
import com.example.be_voluongquang.exception.FileUploadException;
import com.example.be_voluongquang.exception.ResourceNotFoundException;
import com.example.be_voluongquang.repository.FeaturedImageRepository;
import com.example.be_voluongquang.repository.FileArchivalRepository;
import com.example.be_voluongquang.services.FeaturedImageService;
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
public class FeaturedImageServiceImpl implements FeaturedImageService {
    private static final String LABEL = "FeaturedImage";
    private static final String UPLOAD_FOLDER = "images/featured";

    @Autowired
    private FeaturedImageRepository featuredImageRepository;

    @Autowired
    private FileArchivalRepository fileArchivalRepository;

    @Autowired
    private UploadImgImgService uploadImgImgService;

    @Override
    public PagedResponse<FeaturedImageResponseDTO> getFeaturedImagesPage(int page, int size, String search,
            Boolean isDeleted) {
        int safePage = Math.max(0, page);
        int safeSize = size <= 0 ? 10 : size;
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Boolean deletedFilter = isDeleted != null ? isDeleted : Boolean.FALSE;
        Page<FeaturedImageEntity> entityPage;
        if (StringUtils.hasText(search)) {
            entityPage = featuredImageRepository.findByTitleContainingIgnoreCaseAndIsDeleted(search.trim(),
                    deletedFilter, pageable);
        } else {
            entityPage = featuredImageRepository.findByIsDeleted(deletedFilter, pageable);
        }

        Page<FeaturedImageResponseDTO> dtoPage = entityPage.map(this::toResponse);
        return PagedResponse.from(dtoPage);
    }

    @Override
    public FeaturedImageResponseDTO getFeaturedImageById(String id) {
        FeaturedImageEntity entity = featuredImageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(LABEL, "featuredImageId", id));
        if (Boolean.TRUE.equals(entity.getIsDeleted())) {
            throw new ResourceNotFoundException(LABEL, "featuredImageId", id);
        }
        return toResponse(entity);
    }

    @Override
    @Transactional
    public FeaturedImageResponseDTO createFeaturedImage(MultipartFile image, FeaturedImageRequestDTO request) {
        if (image == null || image.isEmpty()) {
            throw new FileUploadException("Featured image file is required");
        }

        FeaturedImageRequestDTO payload = request != null ? request : new FeaturedImageRequestDTO();
        String id = resolveId(payload.getFeaturedImageId());
        if (featuredImageRepository.existsById(id)) {
            throw new IllegalArgumentException("Featured image id already exists");
        }

        String imageUrl = uploadImgImgService.handleSaveUploadFile(image, UPLOAD_FOLDER);
        FileArchivalEntity file = FileArchivalEntity.builder()
                .fileId(UUID.randomUUID().toString())
                .fileUrl(imageUrl)
                .storageProvider("R2")
                .build();
        FileArchivalEntity savedFile = fileArchivalRepository.save(file);

        FeaturedImageEntity entity = FeaturedImageEntity.builder()
                .featuredImageId(id)
                .title(normalizeNullable(payload.getTitle()))
                .linkUrl(normalizeNullable(payload.getLinkUrl()))
                .sortOrder(payload.getSortOrder() != null ? payload.getSortOrder() : 0)
                .isActive(payload.getIsActive() != null ? payload.getIsActive() : Boolean.TRUE)
                .image(savedFile)
                .build();

        return toResponse(featuredImageRepository.save(entity));
    }

    @Override
    @Transactional
    public void deleteFeaturedImage(String id) {
        FeaturedImageEntity entity = featuredImageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(LABEL, "featuredImageId", id));
        entity.setIsDeleted(true);
        featuredImageRepository.save(entity);
    }

    @Override
    @Transactional
    public FeaturedImageResponseDTO restoreFeaturedImage(String id) {
        FeaturedImageEntity entity = featuredImageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(LABEL, "featuredImageId", id));
        if (Boolean.FALSE.equals(entity.getIsDeleted())) {
            return toResponse(entity);
        }
        entity.setIsDeleted(false);
        int updated = featuredImageRepository.softRestoreById(id);
        if (updated == 0) {
            featuredImageRepository.save(entity);
        }
        return toResponse(entity);
    }

    @Override
    public List<FeaturedImageResponseDTO> getActiveFeaturedImages() {
        return featuredImageRepository.findByIsDeletedFalseAndIsActiveTrueOrderBySortOrderAsc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private FeaturedImageResponseDTO toResponse(FeaturedImageEntity entity) {
        FileArchivalEntity image = entity.getImage();
        return FeaturedImageResponseDTO.builder()
                .featuredImageId(entity.getFeaturedImageId())
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

