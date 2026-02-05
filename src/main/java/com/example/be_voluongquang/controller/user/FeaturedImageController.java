package com.example.be_voluongquang.controller.user;

import com.example.be_voluongquang.dto.response.featuredimage.FeaturedImageResponseDTO;
import com.example.be_voluongquang.services.FeaturedImageService;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/featured-image", produces = MediaType.APPLICATION_JSON_VALUE)
public class FeaturedImageController {

    private final FeaturedImageService featuredImageService;

    public FeaturedImageController(FeaturedImageService featuredImageService) {
        this.featuredImageService = featuredImageService;
    }

    @GetMapping
    public List<FeaturedImageResponseDTO> getActiveFeaturedImages() {
        return featuredImageService.getActiveFeaturedImages();
    }

    @GetMapping("/public")
    public List<FeaturedImageResponseDTO> getPublicActiveFeaturedImages() {
        return featuredImageService.getActiveFeaturedImages();
    }
}

