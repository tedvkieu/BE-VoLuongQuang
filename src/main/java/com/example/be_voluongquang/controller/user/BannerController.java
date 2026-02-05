package com.example.be_voluongquang.controller.user;

import com.example.be_voluongquang.dto.response.banner.BannerResponseDTO;
import com.example.be_voluongquang.services.BannerService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/banner", produces = MediaType.APPLICATION_JSON_VALUE)
public class BannerController {

    @Autowired
    private BannerService bannerService;

    @GetMapping
    public List<BannerResponseDTO> getActiveBanners() {
        return bannerService.getActiveBanners();
    }

    @GetMapping("/public")
    public List<BannerResponseDTO> getPublicActiveBanners() {
        return bannerService.getActiveBanners();
    }
}

