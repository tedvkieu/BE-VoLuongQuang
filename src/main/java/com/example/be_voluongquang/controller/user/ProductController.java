package com.example.be_voluongquang.controller.user;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;
import com.example.be_voluongquang.dto.request.product.ProductRequestDTO;
import com.example.be_voluongquang.dto.request.product.ProductSearchRequest;
import com.example.be_voluongquang.dto.response.BrandSimpleDTO;
import com.example.be_voluongquang.dto.response.ProductGroupSimpleDTO;
import com.example.be_voluongquang.dto.response.product.ProductResponseDTO;
import com.example.be_voluongquang.services.BrandService;
import com.example.be_voluongquang.services.ProductGroupService;
import com.example.be_voluongquang.services.ProductService;
import org.springframework.data.domain.Page;

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
    public ResponseEntity<?> getProducts(
            @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(name = "size", required = false, defaultValue = "15") Integer size,
            @RequestParam(name = "search", required = false) String search) {

        return ResponseEntity.ok(productService.getProductsPaged(page, size, search));
    }

    @PostMapping("/search")
    public ResponseEntity<Page<ProductResponseDTO>> searchProducts(@RequestBody ProductSearchRequest request) {
        return ResponseEntity.ok(productService.searchProducts(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable String id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/featured")
    public ResponseEntity<List<ProductResponseDTO>> getFeaturedProducts() {
        return ResponseEntity.ok(productService.getFeaturedProducts());
    }

    @GetMapping("/discount")
    public ResponseEntity<List<ProductResponseDTO>> getAllProductsDiscount() {
        return ResponseEntity.ok(productService.getAllProductsDiscount());
    }

    @GetMapping("/discount-manage")
    public ResponseEntity<Page<ProductResponseDTO>> getDiscountProductsManage(
            @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(name = "size", required = false, defaultValue = "15") Integer size,
            @RequestParam(name = "search", required = false) String search) {
        return ResponseEntity.ok(productService.getDiscountProductsPaged(page, size, search));
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
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @PostMapping
    public ResponseEntity<?> createAProduct(
            @RequestPart(value = "images", required = false) MultipartFile[] images,
            @RequestPart(value = "product") ProductRequestDTO product) {
        ProductResponseDTO savedProduct = productService.createAProduct(images, product);
        return ResponseEntity.ok(savedProduct);
    }

    @PostMapping("/import")
    public ResponseEntity<?> importCsv(@RequestParam("file") MultipartFile file) throws IOException {
        // Gọi service trả về Map<String, Object> gồm success và errors
        Map<String, Object> result = productService.importProductsFromCsv(file);
        return ResponseEntity.ok(result);
    }

    // PUT MAPPING API ----------------------------------------------------------
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @PutMapping(path = "/{id}")
    public ResponseEntity<?> updateAProduct(
            @PathVariable String id,
            @RequestPart(value = "images", required = false) MultipartFile[] images,
            @RequestPart(value = "product") ProductRequestDTO product) {

        ProductResponseDTO savedProduct = productService.updateAProduct(id, images, product);
        return ResponseEntity.ok(savedProduct);
    }

    // DELETE MAPPING API
    // ----T------------------------------------------------------
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAProduct(@PathVariable String id) {
        productService.deleteAProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteMultipleProducts(@RequestBody List<String> ids) {
        productService.deleteMultipleProducts(ids);
        return ResponseEntity.noContent().build();
    }

}
