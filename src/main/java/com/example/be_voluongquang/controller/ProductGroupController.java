package com.example.be_voluongquang.controller;

import com.example.be_voluongquang.dto.response.ProductGroupSimpleDTO;
import com.example.be_voluongquang.services.ProductGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping(path = "/api/product-group", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductGroupController {
    @Autowired
    private ProductGroupService productGroupService;

    @GetMapping()
    public List<ProductGroupSimpleDTO> getAllProductGroups() {
        return productGroupService.getAllProductGroups();
    }
} 