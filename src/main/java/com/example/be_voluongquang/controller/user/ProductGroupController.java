package com.example.be_voluongquang.controller.user;

import com.example.be_voluongquang.dto.request.productgroup.ProductGroupRequestDTO;
import com.example.be_voluongquang.dto.response.ProductGroupSimpleDTO;
import com.example.be_voluongquang.dto.response.productgroup.ProductGroupResponseDTO;
import com.example.be_voluongquang.services.ProductGroupService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ProductGroupResponseDTO> getProductGroupById(@PathVariable String id) {
        return ResponseEntity.ok(productGroupService.getProductGroupById(id));
    }

    @PostMapping()
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ProductGroupResponseDTO> createProductGroup(
            @Valid @RequestBody ProductGroupRequestDTO request) {
        ProductGroupResponseDTO created = productGroupService.createProductGroup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ProductGroupResponseDTO> updateProductGroup(
            @PathVariable String id,
            @Valid @RequestBody ProductGroupRequestDTO request) {
        return ResponseEntity.ok(productGroupService.updateProductGroup(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<Void> deleteProductGroup(@PathVariable String id) {
        productGroupService.deleteProductGroup(id);
        return ResponseEntity.noContent().build();
    }
}
