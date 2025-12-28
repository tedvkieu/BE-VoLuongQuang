package com.example.be_voluongquang.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.be_voluongquang.dto.request.product.ProductRequestDTO;
import com.example.be_voluongquang.dto.request.product.ProductSearchRequest;
import com.example.be_voluongquang.dto.response.BrandSimpleDTO;
import com.example.be_voluongquang.dto.response.CategorySimpleDTO;
import com.example.be_voluongquang.dto.response.ProductGroupSimpleDTO;
import com.example.be_voluongquang.dto.response.product.ProductResponseDTO;
import com.example.be_voluongquang.services.BrandService;
import com.example.be_voluongquang.services.CategoryService;
import com.example.be_voluongquang.services.ProductGroupService;
import com.example.be_voluongquang.services.ProductService;

@RestController
@RequestMapping(path = "/api/admin/product", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
public class AdminProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private ProductGroupService productGroupService;
    @Autowired
    private CategoryService categoryService;

    // READ --------------------------------------------------------------------

    @GetMapping
    public ResponseEntity<Page<ProductResponseDTO>> getProducts(
            @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(name = "size", required = false, defaultValue = "15") Integer size,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "brandIds", required = false) List<String> brandIds,
            @RequestParam(name = "brand_id", required = false) List<String> brandIdsSnake,
            @RequestParam(name = "categoryIds", required = false) List<String> categoryIds,
            @RequestParam(name = "category_id", required = false) List<String> categoryIdsSnake,
            @RequestParam(name = "productGroupIds", required = false) List<String> productGroupIds,
            @RequestParam(name = "product_group_id", required = false) List<String> productGroupIdsSnake) {

        ProductSearchRequest request = new ProductSearchRequest();
        request.setPage(page);
        request.setSize(size);
        request.setSearch(search);
        request.setBrandIds(firstNonEmpty(brandIds, brandIdsSnake));
        request.setCategoryIds(firstNonEmpty(categoryIds, categoryIdsSnake));
        request.setProductGroupIds(firstNonEmpty(productGroupIds, productGroupIdsSnake));

        return ResponseEntity.ok(productService.searchProducts(request));

    }

    @PostMapping("/search")
    public ResponseEntity<Page<ProductResponseDTO>> searchProducts(
            @RequestBody ProductSearchRequest searchRequest) {
        return ResponseEntity.ok(productService.searchProducts(searchRequest));
    }

    @PostMapping(value = "/export/filter", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> exportProductsWithFilter(
            @RequestBody(required = false) ProductSearchRequest searchRequest) {
        byte[] fileContent = productService.exportProducts(searchRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=products_filtered.xlsx");
        headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        return ResponseEntity.ok()
                .headers(headers)
                .body(fileContent);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(
            @PathVariable String id) {
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

    @GetMapping("/categories")
    public List<CategorySimpleDTO> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping("/filters")
    public ResponseEntity<Map<String, Object>> getFilterOptions() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("brands", brandService.getAllBrands());
        payload.put("categories", categoryService.getAllCategories());
        payload.put("productGroups", productGroupService.getAllProductGroups());
        return ResponseEntity.ok(payload);
    }

    // WRITE -------------------------------------------------------------------

    @PostMapping
    public ResponseEntity<?> createAProduct(
            @RequestPart(value = "images", required = false) MultipartFile[] images,
            @RequestPart(value = "product") ProductRequestDTO product) {
        ProductResponseDTO savedProduct = productService.createAProduct(images, product);
        return ResponseEntity.ok(savedProduct);
    }

    @PostMapping("/import")
    public ResponseEntity<?> importCsv(
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(productService.importProductsFromCsv(file));
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<?> updateAProduct(
            @PathVariable String id,
            @RequestPart(value = "images", required = false) MultipartFile[] images,
            @RequestPart(value = "product") ProductRequestDTO product) {
        ProductResponseDTO savedProduct = productService.updateAProduct(id, images, product);
        return ResponseEntity.ok(savedProduct);
    }

    @PatchMapping(path = "/{id}/featured")
    public ResponseEntity<ProductResponseDTO> updateFeatured(
            @PathVariable String id,
            @RequestBody FeaturedToggleRequest request) {
        ProductResponseDTO updated = productService.updateFeatured(id, request.isFeatured());
        return ResponseEntity.ok(updated);
    }

    @PatchMapping(path = "/{id}/discount")
    public ResponseEntity<ProductResponseDTO> updateDiscount(
            @PathVariable String id,
            @RequestBody DiscountUpdateRequest request) {
        ProductResponseDTO updated = productService.updateDiscount(id, request.discountPercent());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAProduct(@PathVariable String id) {
        productService.deleteAProduct(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteMultipleProducts(
            @RequestBody List<String> ids) {
        productService.deleteMultipleProducts(ids);
        return ResponseEntity.noContent().build();
    }

    public record FeaturedToggleRequest(boolean isFeatured) {
    }

    public record DiscountUpdateRequest(Integer discountPercent) {
    }

    private List<String> firstNonEmpty(List<String> primary, List<String> fallback) {
        if (primary != null && !primary.isEmpty()) {
            return primary;
        }
        if (fallback != null && !fallback.isEmpty()) {
            return fallback;
        }
        return null;
    }
}
