package com.example.be_voluongquang.controller.admin;

import com.example.be_voluongquang.dto.response.PagedResponse;
import com.example.be_voluongquang.dto.response.productgroup.ProductGroupResponseDTO;
import com.example.be_voluongquang.services.ProductGroupService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/admin/product-group", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
public class AdminProductGroupController {

    private final ProductGroupService productGroupService;

    public AdminProductGroupController(ProductGroupService productGroupService) {
        this.productGroupService = productGroupService;
    }

    @GetMapping
    public PagedResponse<ProductGroupResponseDTO> getGroups(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String search) {

        int safePage = page != null && page >= 0 ? page : 0;
        int safeSize = size != null && size > 0 ? size : 10;
        return productGroupService.getProductGroupsPage(safePage, safeSize, search);
    }
}
