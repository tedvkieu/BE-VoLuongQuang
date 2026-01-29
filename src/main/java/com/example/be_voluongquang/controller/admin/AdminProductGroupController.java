package com.example.be_voluongquang.controller.admin;

import com.example.be_voluongquang.dto.request.productgroup.ProductGroupRequestDTO;
import com.example.be_voluongquang.dto.response.PagedResponse;
import com.example.be_voluongquang.dto.response.productgroup.ProductGroupResponseDTO;
import com.example.be_voluongquang.services.ProductGroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isDeleted) {

        int safePage = page != null && page >= 0 ? page : 0;
        int safeSize = size != null && size > 0 ? size : 10;
        return productGroupService.getProductGroupsPage(safePage, safeSize, search, isDeleted);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductGroupResponseDTO> getProductGroupById(@PathVariable String id) {
        return ResponseEntity.ok(productGroupService.getProductGroupById(id));
    }

    @PostMapping
    public ResponseEntity<ProductGroupResponseDTO> createProductGroup(
            @Valid @RequestBody ProductGroupRequestDTO request) {
        ProductGroupResponseDTO created = productGroupService.createProductGroup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductGroupResponseDTO> updateProductGroup(
            @PathVariable String id,
            @Valid @RequestBody ProductGroupRequestDTO request) {
        return ResponseEntity.ok(productGroupService.updateProductGroup(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductGroup(@PathVariable String id) {
        productGroupService.deleteProductGroup(id);
        return ResponseEntity.noContent().build();
    }
}
