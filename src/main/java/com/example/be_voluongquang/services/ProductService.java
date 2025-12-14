package com.example.be_voluongquang.services;

import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;
import com.example.be_voluongquang.dto.request.product.ProductRequestDTO;
import com.example.be_voluongquang.dto.request.product.ProductSearchRequest;
import com.example.be_voluongquang.dto.response.product.ProductResponseDTO;
import org.springframework.data.domain.Page;

public interface ProductService {

    // Service for GET method ----------------------------------
    List<ProductResponseDTO> getFeaturedProducts();

    List<ProductResponseDTO> getAllProductsDiscount();

    List<ProductResponseDTO> getAllProduct();

    ProductResponseDTO getProductById(String id);

    /**
     * Lấy danh sách product theo trang, sắp xếp mới nhất theo createdAt DESC.
     * Hỗ trợ tìm kiếm theo tên/mô tả qua tham số search (tùy chọn).
     */
    Page<ProductResponseDTO> getProductsPaged(int page, int size, String search);

    /**
     * Lấy danh sách product có discount theo trang và hỗ trợ tìm kiếm.
     */
    Page<ProductResponseDTO> getDiscountProductsPaged(int page, int size, String search);

    /**
     * Lấy danh sách product hỗ trợ phân trang + lọc + tìm kiếm trong một API POST.
     */
    Page<ProductResponseDTO> searchProducts(ProductSearchRequest request);

    // Service for POST method ----------------------------------
    ProductResponseDTO createAProduct(MultipartFile[] images, ProductRequestDTO product);

    Map<String, Object> importProductsFromCsv(MultipartFile file);

    // Service for PUT method ----------------------------------
    ProductResponseDTO updateAProduct(String id, MultipartFile[] images, ProductRequestDTO product);

    ProductResponseDTO updateFeatured(String id, boolean isFeatured);

    ProductResponseDTO updateDiscount(String id, Integer discountPercent);

    // Service for DELETE method ----------------------------------
    ProductResponseDTO deleteAProduct(String id);

    public void deleteMultipleProducts(List<String> ids);
}
