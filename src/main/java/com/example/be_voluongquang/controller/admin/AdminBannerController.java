package com.example.be_voluongquang.controller.admin;

import com.example.be_voluongquang.dto.request.banner.BannerRequestDTO;
import com.example.be_voluongquang.dto.response.PagedResponse;
import com.example.be_voluongquang.dto.response.banner.BannerResponseDTO;
import com.example.be_voluongquang.services.BannerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = "/api/admin/banner", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
public class AdminBannerController {

    private final BannerService bannerService;

    public AdminBannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @GetMapping
    public PagedResponse<BannerResponseDTO> getBanners(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isDeleted) {

        int safePage = page != null && page >= 0 ? page : 0;
        int safeSize = size != null && size > 0 ? size : 10;
        return bannerService.getBannersPage(safePage, safeSize, search, isDeleted);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BannerResponseDTO> getBannerById(@PathVariable String id) {
        return ResponseEntity.ok(bannerService.getBannerById(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BannerResponseDTO> createBanner(
            @RequestPart("image") MultipartFile image,
            @RequestPart(value = "banner", required = false) BannerRequestDTO banner) {
        BannerResponseDTO created = bannerService.createBanner(image, banner);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BannerResponseDTO> updateBanner(
            @PathVariable String id,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "banner", required = false) BannerRequestDTO banner) {
        return ResponseEntity.ok(bannerService.updateBanner(id, image, banner));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBanner(@PathVariable String id) {
        bannerService.deleteBanner(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(path = "/{id}/restore")
    public ResponseEntity<BannerResponseDTO> restoreBanner(@PathVariable String id) {
        return ResponseEntity.ok(bannerService.restoreBanner(id));
    }
}
