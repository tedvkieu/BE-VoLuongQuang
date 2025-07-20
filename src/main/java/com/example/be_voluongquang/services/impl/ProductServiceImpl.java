package com.example.be_voluongquang.services.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;
import com.example.be_voluongquang.dto.request.product.ProductRequestDTO;
import com.example.be_voluongquang.dto.response.product.ProductResponseDTO;
import com.example.be_voluongquang.entity.BrandEntity;
import com.example.be_voluongquang.entity.CategoryEntity;
import com.example.be_voluongquang.entity.ProductEntity;
import com.example.be_voluongquang.entity.ProductGroupEntity;
import com.example.be_voluongquang.exception.ProductAlreadyExistsException;
import com.example.be_voluongquang.exception.ResourceNotFoundException;
import com.example.be_voluongquang.mapper.ProductMapper;
import com.example.be_voluongquang.repository.BrandRepository;
import com.example.be_voluongquang.repository.CategoryRepository;
import com.example.be_voluongquang.repository.ProductGroupRepository;
import com.example.be_voluongquang.repository.ProductRepository;
import com.example.be_voluongquang.services.ProductService;
import com.example.be_voluongquang.services.app.UploadImgImgService;
import com.example.be_voluongquang.utils.ImageNamingUtil;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductGroupRepository productGroupRepository;
    private final UploadImgImgService uploadImgImgService;

    public ProductServiceImpl(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          BrandRepository brandRepository,
                          ProductGroupRepository productGroupRepository,
                          UploadImgImgService uploadImgImgService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.productGroupRepository = productGroupRepository;
        this.uploadImgImgService = uploadImgImgService;
    }


    // Service Impl for GET Method -----------------------------------------

    @Override
    public List<ProductResponseDTO> getAllProduct(){
        return productMapper.toDtoList(productRepository.findAll());
    }

    @Override
    public List<ProductResponseDTO> getFeaturedProducts() {
        return productMapper.toDtoList(productRepository.findAllByIsFeatured(true));
    }

    @Override
    public List<ProductResponseDTO> getAllProductsDiscount() {
        return productMapper.toDtoList(productRepository.findTop4ByOrderByDiscountPercentDesc());
    }

    // Service Impl for POST Method -----------------------------------------

    @Override
    public ProductResponseDTO createAProduct(MultipartFile[] images, ProductRequestDTO dto){
        
        log.info("=== DEBUG: Starting createAProduct ===");
        log.info("Product DTO: {}", dto);
        log.info("Images array: {}", images);
        
        if (images != null) {
            log.info("Number of images received: {}", images.length);
            for (int i = 0; i < images.length; i++) {
                MultipartFile img = images[i];
                if (img != null) {
                    log.info("Image[{}]: name={}, size={}, contentType={}, isEmpty={}", 
                        i, img.getOriginalFilename(), img.getSize(), img.getContentType(), img.isEmpty());
                } else {
                    log.info("Image[{}]: null", i);
                }
            }
        } else {
            log.info("Images array is null");
        }
        
        if (dto.getProductId() != null && !dto.getProductId().trim().isEmpty()) {
            if (productRepository.existsById(dto.getProductId())) {
                throw new ProductAlreadyExistsException(dto.getProductId());
            }
        }
        
        BrandEntity brand = null;
        if (dto.getBrandId() != null) {
            brand = brandRepository.findById(dto.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand", "brandId", dto.getBrandId()));
        }

        CategoryEntity category = null;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", dto.getCategoryId()));
        }

        ProductGroupEntity productGroup = null;
        if (dto.getProductGroupId() != null) {
            productGroup = productGroupRepository.findById(dto.getProductGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product Group", "productGroupId", dto.getProductGroupId()));
        }

        // --- Upload nhiều ảnh (nếu có) ---
        String imageUrl = null;
        if (images != null && images.length > 0) {
            log.info("=== DEBUG: Starting image upload process ===");
            StringBuilder imageUrls = new StringBuilder();
            String finalPath = "images/product/" + ImageNamingUtil.getFolderByCategoryId(dto.getCategoryId());
            log.info("Final path for images: {}", finalPath);
            
            for (int i = 0; i < images.length; i++) {
                MultipartFile image = images[i];
                log.info("Processing image[{}]: {}", i, image != null ? image.getOriginalFilename() : "null");
                
                if (image != null && !image.isEmpty()) {
                    try {
                        log.info("Uploading image[{}]: {}", i, image.getOriginalFilename());
                        String nameImg = uploadImgImgService.handleSaveUploadFile(image, finalPath);
                        log.info("Uploaded image name: {}", nameImg);
                        
                        String finalImage = ImageNamingUtil.buildFinalImagePath(nameImg, dto.getCategoryId());
                        log.info("Final image path: {}", finalImage);
                        
                        if (i > 0) {
                            imageUrls.append(",");
                        }
                        imageUrls.append(finalImage);
                        log.info("Current imageUrls string: {}", imageUrls.toString());
                    } catch (Exception e) {
                        log.error("Error uploading image[{}]: {}", i, e.getMessage(), e);
                    }
                } else {
                    log.info("Skipping image[{}]: null or empty", i);
                }
            }
            
            if (imageUrls.length() > 0) {
                imageUrl = imageUrls.toString();
                log.info("Final imageUrl: {}", imageUrl);
            } else {
                log.info("No valid images were uploaded");
            }
        } else {
            log.info("No images provided or images array is empty");
        }
        
        log.info("Creating product with imageUrl: {}", imageUrl);
        ProductEntity product = ProductMapper.toEntity(dto, brand, category, productGroup, imageUrl);
        productRepository.save(product);
        
        log.info("Product saved successfully with ID: {}", product.getProductId());
        return productMapper.toDTO(product);
    }

    // Service Impl for PUT Method -----------------------------------------

    @Override
    public ProductResponseDTO updateAProduct(String id, MultipartFile[] images, ProductRequestDTO dto){
        
        log.info("=== DEBUG: Starting updateAProduct ===");
        log.info("Product ID: {}", id);
        log.info("Product DTO: {}", dto);
        log.info("Images array: {}", images);
        
        if (images != null) {
            log.info("Number of images received: {}", images.length);
            for (int i = 0; i < images.length; i++) {
                MultipartFile img = images[i];
                if (img != null) {
                    log.info("Image[{}]: name={}, size={}, contentType={}, isEmpty={}", 
                        i, img.getOriginalFilename(), img.getSize(), img.getContentType(), img.isEmpty());
                } else {
                    log.info("Image[{}]: null", i);
                }
            }
        } else {
            log.info("Images array is null");
        }
        
        // Kiểm tra product có tồn tại không trước khi update
        ProductEntity existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", id));
        
        log.info("Existing product found: {}", existingProduct.getProductId());
        log.info("Existing product imageUrl: {}", existingProduct.getImageUrl());
        
        BrandEntity brand = null;
        if (dto.getBrandId() != null) {
            brand = brandRepository.findById(dto.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand", "brandId", dto.getBrandId()));
        }

        CategoryEntity category = null;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", dto.getCategoryId()));
        }

        ProductGroupEntity productGroup = null;
        if (dto.getProductGroupId() != null) {
            productGroup = productGroupRepository.findById(dto.getProductGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product Group", "productGroupId", dto.getProductGroupId()));
        }

        // --- Xử lý imageUrl từ DTO và upload ảnh mới ---
        String imageUrl = dto.getImageUrl(); // Lấy imageUrl từ DTO (client đã xử lý việc xóa ảnh)
        log.info("ImageUrl from DTO: {}", imageUrl);
        
        // Upload ảnh mới nếu có và append vào imageUrl
        if (images != null && images.length > 0) {
            log.info("=== DEBUG: Starting image upload process for update ===");
            StringBuilder imageUrls = new StringBuilder();
            
            // Bắt đầu với imageUrl từ DTO (nếu có)
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                imageUrls.append(imageUrl);
                log.info("Starting with existing imageUrl: {}", imageUrl);
            }
            
            String finalPath = "images/product/" + ImageNamingUtil.getFolderByCategoryId(dto.getCategoryId());
            log.info("Final path for new images: {}", finalPath);
            
            for (int i = 0; i < images.length; i++) {
                MultipartFile image = images[i];
                log.info("Processing new image[{}]: {}", i, image != null ? image.getOriginalFilename() : "null");
                
                if (image != null && !image.isEmpty()) {
                    try {
                        log.info("Uploading new image[{}]: {}", i, image.getOriginalFilename());
                        String nameImg = uploadImgImgService.handleSaveUploadFile(image, finalPath);
                        log.info("Uploaded new image name: {}", nameImg);
                        
                        String finalImage = ImageNamingUtil.buildFinalImagePath(nameImg, dto.getCategoryId());
                        log.info("Final new image path: {}", finalImage);
                        
                        // Thêm dấu phẩy nếu đã có ảnh trước đó
                        if (imageUrls.length() > 0) {
                            imageUrls.append(",");
                        }
                        imageUrls.append(finalImage);
                        log.info("Current imageUrls string after adding new image: {}", imageUrls.toString());
                    } catch (Exception e) {
                        log.error("Error uploading new image[{}]: {}", i, e.getMessage(), e);
                    }
                } else {
                    log.info("Skipping new image[{}]: null or empty", i);
                }
            }
            
            if (imageUrls.length() > 0) {
                imageUrl = imageUrls.toString();
                log.info("Final imageUrl after adding new images: {}", imageUrl);
            } else {
                log.info("No new images were uploaded, keeping imageUrl from DTO");
            }
        } else {
            log.info("No new images provided, using imageUrl from DTO: {}", imageUrl);
        }
        
        // Cập nhật thông tin product
        log.info("Creating updated product with final imageUrl: {}", imageUrl);
        ProductEntity updatedProduct = ProductMapper.toEntity(dto, brand, category, productGroup, imageUrl);
        updatedProduct.setProductId(id); // Đảm bảo giữ nguyên ID
        updatedProduct.setCreatedAt(existingProduct.getCreatedAt()); // Giữ nguyên thời gian tạo
        
        ProductEntity savedProduct = productRepository.save(updatedProduct);
        
        log.info("Product updated successfully with ID: {}", savedProduct.getProductId());
        log.info("Final saved product imageUrl: {}", savedProduct.getImageUrl());
        
        return productMapper.toDTO(savedProduct);
    }
}
