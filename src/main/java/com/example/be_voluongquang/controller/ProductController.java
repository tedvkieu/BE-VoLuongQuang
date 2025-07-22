package com.example.be_voluongquang.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.example.be_voluongquang.dto.request.product.ProductRequestDTO;
import com.example.be_voluongquang.dto.response.BrandSimpleDTO;
import com.example.be_voluongquang.dto.response.ProductGroupSimpleDTO;
import com.example.be_voluongquang.dto.response.product.ProductResponseDTO;
import com.example.be_voluongquang.services.BrandService;
import com.example.be_voluongquang.services.ProductGroupService;
import com.example.be_voluongquang.services.ProductService;

@RestController
@RequestMapping(path = "/api/product", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private ProductGroupService productGroupService;

    // GET MAPPING API ----------------------------------------------------------

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAllProduct() {
        return ResponseEntity.ok(productService.getAllProduct());
    }

    @GetMapping("/featured")
    public ResponseEntity<List<ProductResponseDTO>> getFeaturedProducts() {
        return ResponseEntity.ok(productService.getFeaturedProducts());
    }

    @GetMapping("/discount")
    public ResponseEntity<List<ProductResponseDTO>> getAllProductsDiscount() {
        return ResponseEntity.ok(productService.getAllProductsDiscount());
    }

    @GetMapping("/brands")
    public List<BrandSimpleDTO> getAllBrands() {
        return brandService.getAllBrands();
    }

    @GetMapping("/product-groups")
    public List<ProductGroupSimpleDTO> getAllProductGroups() {
        return productGroupService.getAllProductGroups();
    }

    // POST MAPPING API ----------------------------------------------------------
    @PostMapping
    public ResponseEntity<?> createAProduct(
            @RequestPart(value = "images", required = false) MultipartFile[] images,
            @RequestPart(value = "product") ProductRequestDTO product) {

        System.out.println("checck imageS: " + images);
        System.out.println("check product: " + product);

        ProductResponseDTO savedProduct = productService.createAProduct(images, product);
        return ResponseEntity.ok(savedProduct);
    }

    // PUT MAPPING API ----------------------------------------------------------
    @PutMapping(path = "/{id}")
    public ResponseEntity<?> updateAProduct(
            @PathVariable String id,
            @RequestPart(value = "images", required = false) MultipartFile[] images,
            @RequestPart(value = "product") ProductRequestDTO product) {

        System.out.println("checck imageS: " + images);
        System.out.println("check product: " + product);
        ProductResponseDTO savedProduct = productService.updateAProduct(id, images, product);
        return ResponseEntity.ok(savedProduct);
    }

    // DELETE MAPPING API
    // ----T------------------------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAProduct(@PathVariable String id) {
        productService.deleteAProduct(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteMultipleProducts(@RequestBody List<String> ids) {
        productService.deleteMultipleProducts(ids);
        return ResponseEntity.noContent().build();
    }

}
