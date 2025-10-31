package com.example.be_voluongquang.services.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;
import com.example.be_voluongquang.dto.request.product.ImportErrorDTO;
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
import com.example.be_voluongquang.utils.CsvParserUtils;
import com.example.be_voluongquang.utils.ImageNamingUtil;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
    public List<ProductResponseDTO> getAllProduct() {
        return productMapper.toDtoList(productRepository.findAll());
    }

    @Override
    public ProductResponseDTO getProductById(String id) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", id));
        return productMapper.toDTO(product);
    }

    @Override
    public List<ProductResponseDTO> getFeaturedProducts() {
        return productMapper.toDtoList(productRepository.findAllByIsFeatured(true));
    }

    @Override
    public List<ProductResponseDTO> getAllProductsDiscount() {
        return productMapper.toDtoList(productRepository.findTop4ByOrderByDiscountPercentDesc());
    }

    @Override
    public Page<ProductResponseDTO> getProductsPaged(int page, int size, String search) {
        int safePage = Math.max(0, page);
        int safeSize = size <= 0 ? 15 : size;
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<com.example.be_voluongquang.entity.ProductEntity> entityPage;
        if (search != null && !search.trim().isEmpty()) {
            String term = search.trim();
            entityPage = productRepository.findByNameOrDescriptionContaining(term, pageable);
        } else {
            entityPage = productRepository.findAll(pageable);
        }

        return entityPage.map(productMapper::toDTO);
    }

    @Override
    public Page<ProductResponseDTO> getDiscountProductsPaged(int page, int size, String search) {
        int safePage = Math.max(0, page);
        int safeSize = size <= 0 ? 15 : size;
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "discountPercent"));

        String processedSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        Page<ProductEntity> entityPage;
        if (processedSearch != null) {
            entityPage = productRepository.searchDiscountedProducts(0, processedSearch, pageable);
        } else {
            entityPage = productRepository.findByDiscountPercentGreaterThan(0, pageable);
        }

        return entityPage.map(productMapper::toDTO);
    }

    // Service Impl for POST Method -----------------------------------------

    @Override
    public ProductResponseDTO createAProduct(MultipartFile[] images, ProductRequestDTO dto) {

        log.info("=== DEBUG: Starting createAProduct ===");
        log.info("Product DTO: {}", dto);

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
                    .orElseThrow(() -> new ResourceNotFoundException("Product Group", "productGroupId",
                            dto.getProductGroupId()));
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

    @Override
    public Map<String, Object> importProductsFromCsv(MultipartFile file) {
        List<ProductEntity> products = new ArrayList<>();
        List<ImportErrorDTO> errorList = new ArrayList<>();
        List<ProductResponseDTO> successList = new ArrayList<>();

        String filename = file.getOriginalFilename();
        if (filename == null ||
                !(filename.toLowerCase().endsWith(".csv") || filename.toLowerCase().endsWith(".xlsx"))) {
            throw new IllegalArgumentException("Chỉ chấp nhận file .csv hoặc .xlsx");
        }

        try {
            if (filename.toLowerCase().endsWith(".csv")) {
                // Đọc file CSV như cũ
                try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                    CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build();
                    List<String[]> rows = csvReader.readAll();

                    for (String[] row : rows) {
                        processRow(row, products, errorList);
                    }
                }
            } else if (filename.toLowerCase().endsWith(".xlsx")) {
                // Đọc file Excel
                try (InputStream is = file.getInputStream()) {
                    Workbook workbook = new XSSFWorkbook(is);
                    Sheet sheet = workbook.getSheetAt(0);
                    boolean isFirstRow = true;
                    for (org.apache.poi.ss.usermodel.Row excelRow : sheet) {
                        if (isFirstRow) {
                            isFirstRow = false;
                            continue;
                        } // Bỏ qua header
                        String[] row = new String[16];
                        for (int i = 0; i < 16; i++) {
                            org.apache.poi.ss.usermodel.Cell cell = excelRow.getCell(i);
                            row[i] = (cell == null) ? null : getCellStringValue(cell);
                        }
                        processRow(row, products, errorList);
                    }
                    workbook.close();
                }
            }
            // Lưu các bản ghi hợp lệ
            List<ProductEntity> savedProducts = productRepository.saveAll(products);
            // Chuyển sang DTO để trả về FE
            for (ProductEntity entity : savedProducts) {
                successList.add(productMapper.toDTO(entity));
            }
        } catch (IOException | CsvException e) {
            errorList.add(ImportErrorDTO.builder()
                    .productId("N/A")
                    .errorMessage("File read error: " + e.getMessage())
                    .build());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", successList);
        result.put("errors", errorList);
        return result;
    }

    // Service Impl for PUT Method -----------------------------------------

    @Override
    @Transactional
    public ProductResponseDTO updateAProduct(String id, MultipartFile[] images, ProductRequestDTO dto) {
        log.info("=== DEBUG: Starting updateAProduct ===");
        log.info("Product ID: {}", id);
        log.info("ImageUrl from DTO (existing images to keep): {}", dto.getImageUrl());

        if (images != null) {
            log.info("Number of new images to upload: {}", images.length);
            for (int i = 0; i < images.length; i++) {
                MultipartFile img = images[i];
                if (img != null && !img.isEmpty()) {
                    log.info("New image[{}]: name={}, size={}, contentType={}",
                            i, img.getOriginalFilename(), img.getSize(), img.getContentType());
                } else {
                    log.info("New image[{}]: null or empty", i);
                }
            }
        } else {
            log.info("No new images to upload");
        }

        // Kiểm tra product có tồn tại không trước khi update
        ProductEntity existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", id));

        log.info("Existing product found: {}", existingProduct.getProductId());
        log.info("Current product imageUrl in DB: {}", existingProduct.getImageUrl());

        // Validate related entities using DTO if provided; otherwise keep existing
        BrandEntity brand = null;
        if (dto.getBrandId() != null && !dto.getBrandId().trim().isEmpty()) {
            brand = brandRepository.findById(dto.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand", "brandId", dto.getBrandId()));
        } else {
            brand = existingProduct.getBrand();
        }

        CategoryEntity category = null;
        if (dto.getCategoryId() != null && !dto.getCategoryId().trim().isEmpty()) {
            category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", dto.getCategoryId()));
        } else {
            category = existingProduct.getCategory();
        }

        ProductGroupEntity productGroup = null;
        if (dto.getProductGroupId() != null && !dto.getProductGroupId().trim().isEmpty()) {
            productGroup = productGroupRepository.findById(dto.getProductGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product Group", "productGroupId",
                            dto.getProductGroupId()));
        } else {
            productGroup = existingProduct.getProductGroup();
        }

        // === XỬ LÝ HÌNH ẢNH THEO LOGIC MỚI ===
        String finalImageUrl = processImageUpdate(dto.getImageUrl(), images,
                dto.getCategoryId() != null && !dto.getCategoryId().trim().isEmpty()
                        ? dto.getCategoryId()
                        : (existingProduct.getCategory() != null ? existingProduct.getCategory().getCategoryId()
                                : null));

        log.info("Final imageUrl after processing: {}", finalImageUrl);

        // Cập nhật thông tin product
        ProductEntity updatedProduct = ProductMapper.toEntity(dto, brand, category, productGroup, finalImageUrl);
        updatedProduct.setProductId(id); // Đảm bảo giữ nguyên ID
        updatedProduct.setCreatedAt(existingProduct.getCreatedAt()); // Giữ nguyên thời gian tạo
        if (updatedProduct.getUrlShopee() == null) {
            updatedProduct.setUrlShopee(existingProduct.getUrlShopee());
        }
        if (updatedProduct.getUrlLazada() == null) {
            updatedProduct.setUrlLazada(existingProduct.getUrlLazada());
        }
        if (updatedProduct.getUrlOther() == null) {
            updatedProduct.setUrlOther(existingProduct.getUrlOther());
        }

        ProductEntity savedProduct = productRepository.save(updatedProduct);

        log.info("Product updated successfully with ID: {}", savedProduct.getProductId());
        log.info("Final saved product imageUrl: {}", savedProduct.getImageUrl());

        return productMapper.toDTO(savedProduct);
    }

    /**
     * Xử lý logic update hình ảnh
     * 
     * @param existingImageUrl - Danh sách path ảnh cũ được giữ lại (từ frontend)
     * @param newImages        - Mảng file ảnh mới cần upload
     * @param categoryId       - ID category để tạo đường dẫn upload
     * @return String cuối cùng chứa tất cả path ảnh (cũ + mới) theo thứ tự
     */
    private String processImageUpdate(String finalOrderFromFE, MultipartFile[] newImages, String categoryId) {
        log.info("=== PROCESSING IMAGE UPDATE (ORDER AWARE) === Final order from FE: {}", finalOrderFromFE);

        // 1) Upload new images and collect their final paths
        List<String> newImagePaths = new ArrayList<>();
        if (newImages != null && newImages.length > 0) {
            String uploadPath = "images/product/" + ImageNamingUtil.getFolderByCategoryId(categoryId);
            for (int i = 0; i < newImages.length; i++) {
                MultipartFile image = newImages[i];
                if (image != null && !image.isEmpty()) {
                    try {
                        String savedFileName = uploadImgImgService.handleSaveUploadFile(image, uploadPath);
                        String newImagePath = ImageNamingUtil.buildFinalImagePath(savedFileName, categoryId);
                        newImagePaths.add(newImagePath);
                        log.info("Uploaded new image[{}] as {}", i, newImagePath);
                    } catch (Exception e) {
                        log.error("Error uploading new image[{}]: {}", i, e.getMessage(), e);
                    }
                }
            }
        }

        // 2) Build final order based on FE sequence (existing paths + NEW_IMAGE_i
        // placeholders)
        if (finalOrderFromFE == null || finalOrderFromFE.trim().isEmpty()) {
            log.warn("No final order provided by FE; returning only newly uploaded paths.");
            return String.join(",", newImagePaths);
        }

        String[] tokens = finalOrderFromFE.split(",");
        List<String> result = new ArrayList<>();
        int newIdx = 0;
        for (String raw : tokens) {
            String token = raw.trim();
            if (token.isEmpty())
                continue;
            if (token.startsWith("NEW_IMAGE_")) {
                if (newIdx < newImagePaths.size()) {
                    result.add(newImagePaths.get(newIdx++));
                } else {
                    log.warn("Missing uploaded file for placeholder: {}", token);
                }
            } else {
                // existing path from FE
                result.add(token);
            }
            if (result.size() >= 5)
                break; // enforce max 5
        }

        String finalJoined = String.join(",", result);
        log.info("Final processed imageUrl (ordered): {}", finalJoined);
        return finalJoined;
    }

    /**
     * Utility method để clean up các ảnh không còn được sử dụng (optional)
     * Gọi method này để xóa các file ảnh cũ không còn được reference
     */
    private void cleanupUnusedImages(String oldImageUrl, String newImageUrl) {
        if (oldImageUrl == null || oldImageUrl.trim().isEmpty()) {
            return;
        }

        Set<String> oldPaths = new HashSet<>(Arrays.asList(oldImageUrl.split(",")));
        Set<String> newPaths = new HashSet<>();

        if (newImageUrl != null && !newImageUrl.trim().isEmpty()) {
            newPaths.addAll(Arrays.asList(newImageUrl.split(",")));
        }

        // Tìm các ảnh cũ không còn được sử dụng
        Set<String> imagesToDelete = new HashSet<>(oldPaths);
        imagesToDelete.removeAll(newPaths);

        // Xóa các file không còn sử dụng
        for (String imagePath : imagesToDelete) {
            try {
                // Implement logic xóa file vật lý nếu cần
                log.info("Should delete unused image: {}", imagePath);
                // fileService.deleteFile(imagePath);
            } catch (Exception e) {
                log.warn("Failed to delete unused image {}: {}", imagePath, e.getMessage());
            }
        }
    }

    // Service Impl for PUT Method -----------------------------------------

    @Override
    @Transactional
    public ProductResponseDTO deleteAProduct(String id) {
        log.info("=== DEBUG: Starting deleteAProduct ===");
        log.info("Product ID to delete: {}", id);

        // Kiểm tra product có tồn tại không trước khi xóa
        ProductEntity existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", id));

        log.info("Existing product found: {}", existingProduct.getProductId());

        // Xóa sản phẩm
        productRepository.delete(existingProduct);
        log.info("Product deleted successfully with ID: {}", id);

        return productMapper.toDTO(existingProduct);
    }

    @Override
    public void deleteMultipleProducts(List<String> ids) {
        ids.forEach(this::deleteAProduct);
    }

    // Hàm xử lý từng dòng (dùng chung cho cả csv và xlsx)
    private void processRow(String[] row, List<ProductEntity> products, List<ImportErrorDTO> errorList) {
        String productId = row[0];
        // Kiểm tra trùng trong DB
        boolean existsInDb = productRepository.existsById(productId);
        // Kiểm tra trùng trong danh sách tạm (các dòng đã duyệt trong file)
        boolean existsInBatch = products.stream().anyMatch(p -> p.getProductId().equals(productId));
        if (existsInDb || existsInBatch) {
            errorList.add(ImportErrorDTO.builder()
                    .productId(productId)
                    .errorMessage("Product already exists")
                    .build());
            return;
        }
        try {
            ProductEntity product = new ProductEntity();
            product.setProductId(productId);
            product.setName(row[1]);
            // Kiểm tra null cho các trường id
            product.setProductGroup(
                    (row[2] == null || row[2].trim().isEmpty()) ? null
                            : productGroupRepository.findById(row[2]).orElse(null));
            product.setCategory(
                    (row[3] == null || row[3].trim().isEmpty()) ? null
                            : categoryRepository.findById(row[3]).orElse(null));
            product.setBrand(
                    (row[4] == null || row[4].trim().isEmpty()) ? null : brandRepository.findById(row[4]).orElse(null));
            product.setPrice(CsvParserUtils.parseDouble(row[5]));
            product.setCostPrice(CsvParserUtils.parseDouble(row[6]));
            product.setWholesalePrice(CsvParserUtils.parseDouble(row[7]));
            // Default discount_percent = 0 when CSV cell is empty or invalid
            Integer csvDiscount = CsvParserUtils.parseInteger(row[8]);
            product.setDiscountPercent(csvDiscount != null ? csvDiscount : 0);
            product.setStockQuantity(CsvParserUtils.parseInteger(row[9]));
            product.setWeight(CsvParserUtils.parseDouble(row[10]));
            product.setUnit(row[11]);
            product.setIsFeatured(CsvParserUtils.parseBoolean(row[12]));
            product.setIsActive(CsvParserUtils.parseBoolean(row[13]));
            product.setImageUrl(row[14]);
            product.setDescription(row[15]);

            int createdAtIndex = -1;
            int updatedAtIndex = -1;

            if (row.length >= 21) {
                product.setUrlShopee(row[16]);
                product.setUrlLazada(row[17]);
                product.setUrlOther(row[18]);
                createdAtIndex = 19;
                updatedAtIndex = 20;
            } else {
                if (row.length > 16) {
                    createdAtIndex = 16;
                }
                if (row.length > 17) {
                    updatedAtIndex = 17;
                }
            }

            if (createdAtIndex >= 0 && row[createdAtIndex] != null && !row[createdAtIndex].isEmpty()) {
                product.setCreatedAt(LocalDateTime.parse(row[createdAtIndex]));
            }
            if (updatedAtIndex >= 0 && row[updatedAtIndex] != null && !row[updatedAtIndex].isEmpty()) {
                product.setUpdatedAt(LocalDateTime.parse(row[updatedAtIndex]));
            }
            products.add(product);
        } catch (Exception e) {
            errorList.add(ImportErrorDTO.builder()
                    .productId(productId)
                    .errorMessage("Parsing error: " + e.getMessage())
                    .build());
        }
    }

    // Hàm chuyển cell Excel về String
    private String getCellStringValue(org.apache.poi.ss.usermodel.Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "";
        }
    }
}
