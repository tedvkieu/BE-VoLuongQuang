package com.example.be_voluongquang.controller.admin;

import com.example.be_voluongquang.dto.request.featuredimage.FeaturedImageRequestDTO;
import com.example.be_voluongquang.dto.response.PagedResponse;
import com.example.be_voluongquang.dto.response.featuredimage.FeaturedImageResponseDTO;
import com.example.be_voluongquang.services.FeaturedImageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = "/api/admin/featured-image", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
public class AdminFeaturedImageController {

    private final FeaturedImageService featuredImageService;

    public AdminFeaturedImageController(FeaturedImageService featuredImageService) {
        this.featuredImageService = featuredImageService;
    }

    @GetMapping
    public PagedResponse<FeaturedImageResponseDTO> getFeaturedImages(
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "isDeleted", required = false) Boolean isDeleted) {
        int safePage = page != null && page >= 0 ? page : 0;
        int safeSize = size != null && size > 0 ? size : 10;
        return featuredImageService.getFeaturedImagesPage(safePage, safeSize, search, isDeleted);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeaturedImageResponseDTO> getById(@PathVariable("id") String id) {
        return ResponseEntity.ok(featuredImageService.getFeaturedImageById(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FeaturedImageResponseDTO> create(
            @RequestPart("image") MultipartFile image,
            @RequestPart(value = "featuredImage", required = false) FeaturedImageRequestDTO payload) {
        FeaturedImageResponseDTO created = featuredImageService.createFeaturedImage(image, payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        featuredImageService.deleteFeaturedImage(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<FeaturedImageResponseDTO> restore(@PathVariable("id") String id) {
        return ResponseEntity.ok(featuredImageService.restoreFeaturedImage(id));
    }
}

