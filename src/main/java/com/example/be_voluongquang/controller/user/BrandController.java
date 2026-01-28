package com.example.be_voluongquang.controller.user;

import com.example.be_voluongquang.dto.response.BrandSimpleDTO;
import com.example.be_voluongquang.services.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/api/brand", produces = MediaType.APPLICATION_JSON_VALUE)
public class BrandController {
    @Autowired
    private BrandService brandService;

    @GetMapping
    public List<BrandSimpleDTO> getAllBrands() {
        return brandService.getAllBrands();
    }

    @GetMapping("/public")
    public List<BrandSimpleDTO> getPublicBrands() {
        return brandService.getAllBrands();
    }
}
