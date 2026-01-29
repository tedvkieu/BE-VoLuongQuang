package com.example.be_voluongquang.services.impl;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;
import com.example.be_voluongquang.dto.request.product.ImportErrorDTO;
import com.example.be_voluongquang.dto.request.product.ProductRequestDTO;
import com.example.be_voluongquang.dto.request.product.ProductSearchRequest;
import com.example.be_voluongquang.dto.response.product.ProductResponseDTO;
import com.example.be_voluongquang.entity.BrandEntity;
import com.example.be_voluongquang.entity.CategoryEntity;
import com.example.be_voluongquang.entity.ProductEntity;
import com.example.be_voluongquang.entity.ProductGroupEntity;
import com.example.be_voluongquang.entity.FileArchivalEntity;
import com.example.be_voluongquang.exception.ProductAlreadyExistsException;
import com.example.be_voluongquang.exception.ResourceNotFoundException;
import com.example.be_voluongquang.mapper.ProductMapper;
import com.example.be_voluongquang.repository.BrandRepository;
import com.example.be_voluongquang.repository.CategoryRepository;
import com.example.be_voluongquang.repository.FileArchivalRepository;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import jakarta.persistence.criteria.Predicate;

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
    private final FileArchivalRepository fileArchivalRepository;
    private final ImageNamingUtil imageNamingUtil;

    public ProductServiceImpl(ProductRepository productRepository,
            CategoryRepository categoryRepository,
            BrandRepository brandRepository,
            ProductGroupRepository productGroupRepository,
            UploadImgImgService uploadImgImgService,
            FileArchivalRepository fileArchivalRepository,
            ImageNamingUtil imageNamingUtil) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.productGroupRepository = productGroupRepository;
        this.uploadImgImgService = uploadImgImgService;
        this.fileArchivalRepository = fileArchivalRepository;
        this.imageNamingUtil = imageNamingUtil;
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

    @Override
    public Page<ProductResponseDTO> searchProducts(ProductSearchRequest request) {
        int safePage = request.getPage() != null ? Math.max(0, request.getPage()) : 0;
        int safeSize = (request.getSize() != null && request.getSize() > 0) ? request.getSize() : 15;
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<ProductEntity> specification = buildProductSpecification(request);
        Page<ProductEntity> entityPage = productRepository.findAll(specification, pageable);

        return entityPage.map(productMapper::toDTO);
    }

    @Override
    public byte[] exportProducts(ProductSearchRequest request) {
        ProductSearchRequest safeRequest = (request != null) ? request : new ProductSearchRequest();
        int safePage = safeRequest.getPage() != null ? Math.max(0, safeRequest.getPage()) : 0;
        int safeSize = (safeRequest.getSize() != null && safeRequest.getSize() > 0) ? safeRequest.getSize() : 15;
        Specification<ProductEntity> specification = buildProductSpecification(safeRequest);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<ProductEntity> products = productRepository.findAll(specification, pageable).getContent();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Products");
            int rowIndex = 0;

            String[] headers = new String[] { "Product ID", "Name", "Product Group ID", "Category ID", "Brand ID",
                    "Price", "Cost Price", "Wholesale Price", "Discount Percent", "Stock Quantity", "Weight", "Unit",
                    "Is Featured", "Is Active", "Image URL", "Description", "URL Shopee", "URL Lazada", "URL Other",
                    "Created At", "Updated At" };

            Row headerRow = sheet.createRow(rowIndex++);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            for (ProductEntity product : products) {
                Row row = sheet.createRow(rowIndex++);
                int col = 0;

                row.createCell(col++).setCellValue(safeString(product.getProductId()));
                row.createCell(col++).setCellValue(safeString(product.getName()));
                row.createCell(col++).setCellValue(
                        product.getProductGroup() != null ? safeString(product.getProductGroup().getGroupId()) : "");
                row.createCell(col++).setCellValue(
                        product.getCategory() != null ? safeString(product.getCategory().getCategoryId()) : "");
                row.createCell(col++).setCellValue(
                        product.getBrand() != null ? safeString(product.getBrand().getBrandId()) : "");
                row.createCell(col++).setCellValue(safeString(product.getPrice()));
                row.createCell(col++).setCellValue(safeString(product.getCostPrice()));
                row.createCell(col++).setCellValue(safeString(product.getWholesalePrice()));
                row.createCell(col++).setCellValue(safeString(product.getDiscountPercent()));
                row.createCell(col++).setCellValue(safeString(product.getStockQuantity()));
                row.createCell(col++).setCellValue(safeString(product.getWeight()));
                row.createCell(col++).setCellValue(safeString(product.getUnit()));
                row.createCell(col++).setCellValue(safeString(product.getIsFeatured()));
                row.createCell(col++).setCellValue(safeString(product.getIsActive()));
                row.createCell(col++).setCellValue(safeString(product.getImageUrl()));
                row.createCell(col++).setCellValue(safeString(product.getDescription()));
                row.createCell(col++).setCellValue(safeString(product.getUrlShopee()));
                row.createCell(col++).setCellValue(safeString(product.getUrlLazada()));
                row.createCell(col++).setCellValue(safeString(product.getUrlOther()));
                row.createCell(col++).setCellValue(
                        product.getCreatedAt() != null ? product.getCreatedAt().toString() : "");
                row.createCell(col++).setCellValue(
                        product.getUpdatedAt() != null ? product.getUpdatedAt().toString() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Failed to export products to Excel", e);
            throw new RuntimeException("Không thể xuất file Excel sản phẩm", e);
        }
    }

    private Specification<ProductEntity> buildProductSpecification(ProductSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(request.getSearch())) {
                String term = "%" + request.getSearch().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), term),
                        cb.like(cb.lower(root.get("description")), term),
                        cb.like(cb.lower(root.get("productId")), term)));
            }

            if (!CollectionUtils.isEmpty(request.getBrandIds())) {
                predicates.add(root.get("brand").get("brandId").in(request.getBrandIds()));
            }
            if (!CollectionUtils.isEmpty(request.getCategoryIds())) {
                predicates.add(root.get("category").get("categoryId").in(request.getCategoryIds()));
            }
            if (!CollectionUtils.isEmpty(request.getProductGroupIds())) {
                predicates.add(root.get("productGroup").get("groupId").in(request.getProductGroupIds()));
            }

            Double minPrice = request.getMinPrice();
            Double maxPrice = request.getMaxPrice();
            if (minPrice != null && maxPrice != null && maxPrice < minPrice) {
                double temp = minPrice;
                minPrice = maxPrice;
                maxPrice = temp;
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (request.getMinDiscount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("discountPercent"), request.getMinDiscount()));
            }
            if (request.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), request.getIsActive()));
            }
            if (request.getIsFeatured() != null) {
                predicates.add(cb.equal(root.get("isFeatured"), request.getIsFeatured()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // Service Impl for POST Method -----------------------------------------

    @Override
    public ProductResponseDTO createAProduct(MultipartFile[] images, ProductRequestDTO dto) {

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
        if (StringUtils.hasText(dto.getProductGroupId())) {
            String groupId = dto.getProductGroupId().trim();
            productGroup = productGroupRepository.findById(groupId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product Group", "groupId", groupId));
        }

        // --- Upload nhiều ảnh (nếu có) ---
        String imageUrl = null;
        if (images != null && images.length > 0) {
            StringBuilder imageUrls = new StringBuilder();
            String finalPath = "images/product/" + imageNamingUtil.getFolderByCategoryId(dto.getCategoryId());

            for (int i = 0; i < images.length; i++) {
                MultipartFile image = images[i];

                if (image != null && !image.isEmpty()) {
                    try {
                        String nameImg = uploadImgImgService.handleSaveUploadFile(image, finalPath);

                        String finalImage = normalizeUploadedImage(nameImg, dto.getCategoryId());

                        if (i > 0) {
                            imageUrls.append(",");
                        }
                        imageUrls.append(finalImage);
                    } catch (Exception e) {
                        log.error("Error uploading image[{}]: {}", i, e.getMessage(), e);
                    }
                }
            }

            if (imageUrls.length() > 0) {
                imageUrl = imageUrls.toString();
            }
        }

        ProductEntity product = ProductMapper.toEntity(dto, brand, category, productGroup, imageUrl);
        ProductEntity savedProduct = productRepository.save(product);
        syncFileArchivals(savedProduct, imageUrl);

        return productMapper.toDTO(savedProduct);
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
        // Kiểm tra product có tồn tại không trước khi update
        ProductEntity existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", id));

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
        if (StringUtils.hasText(dto.getProductGroupId())) {
            String groupId = dto.getProductGroupId().trim();
            productGroup = productGroupRepository.findById(groupId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product Group", "groupId", groupId));
        } else {
            productGroup = existingProduct.getProductGroup();
        }

        // === XỬ LÝ HÌNH ẢNH THEO LOGIC MỚI ===
        String finalImageUrl = processImageUpdate(dto.getImageUrl(), images,
                dto.getCategoryId() != null && !dto.getCategoryId().trim().isEmpty()
                        ? dto.getCategoryId()
                        : (existingProduct.getCategory() != null ? existingProduct.getCategory().getCategoryId()
                                : null));

        if (!StringUtils.hasText(finalImageUrl)) {

            finalImageUrl = existingProduct.getImageUrl();
        }

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
        syncFileArchivals(savedProduct, finalImageUrl);

        return productMapper.toDTO(savedProduct);
    }

    @Override
    @Transactional
    public ProductResponseDTO updateFeatured(String id, boolean isFeatured) {
        ProductEntity existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", id));
        existingProduct.setIsFeatured(isFeatured);
        ProductEntity saved = productRepository.save(existingProduct);
        return productMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public ProductResponseDTO updateDiscount(String id, Integer discountPercent) {
        ProductEntity existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", id));
        int sanitized = discountPercent == null ? 0 : Math.max(0, Math.min(discountPercent, 100));
        existingProduct.setDiscountPercent(sanitized);
        ProductEntity saved = productRepository.save(existingProduct);
        return productMapper.toDTO(saved);
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
        // 1) Upload new images and collect their final paths
        List<String> newImagePaths = new ArrayList<>();
        if (newImages != null && newImages.length > 0) {
            String uploadPath = "images/product/" + imageNamingUtil.getFolderByCategoryId(categoryId);
            for (int i = 0; i < newImages.length; i++) {
                MultipartFile image = newImages[i];
                if (image != null && !image.isEmpty()) {
                    try {
                        String savedFileName = uploadImgImgService.handleSaveUploadFile(image, uploadPath);
                        String newImagePath = normalizeUploadedImage(savedFileName, categoryId);
                        newImagePaths.add(newImagePath);
                    } catch (Exception e) {
                        log.error("Error uploading new image[{}]: {}", i, e.getMessage(), e);
                    }
                }
            }
        }

        // 2) Build final order based on FE sequence (existing paths + NEW_IMAGE_i
        // placeholders)
        if (finalOrderFromFE == null || finalOrderFromFE.trim().isEmpty()) {
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
        return finalJoined;
    }

    private void syncFileArchivals(ProductEntity product, String imageUrl) {
        if (product == null || product.getProductId() == null) {
            return;
        }

        fileArchivalRepository.deleteByProduct_ProductId(product.getProductId());

        if (!StringUtils.hasText(imageUrl)) {
            return;
        }

        String[] parts = imageUrl.split(",");
        List<FileArchivalEntity> entries = new ArrayList<>();
        for (String raw : parts) {
            String trimmed = raw.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            FileArchivalEntity file = FileArchivalEntity.builder()
                    .fileId(java.util.UUID.randomUUID().toString())
                    .fileUrl(trimmed)
                    .storageProvider("R2")
                    .product(product)
                    .build();
            entries.add(file);
        }

        if (!entries.isEmpty()) {
            fileArchivalRepository.saveAll(entries);
        }
    }

    private String normalizeUploadedImage(String uploaded, String categoryId) {
        if (!StringUtils.hasText(uploaded)) {
            return uploaded;
        }
        String trimmed = uploaded.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        if (trimmed.contains("/")) {
            return trimmed;
        }
        return imageNamingUtil.buildFinalImagePath(trimmed, categoryId);
    }

    // Service Impl for PUT Method -----------------------------------------

    @Override
    @Transactional
    public ProductResponseDTO deleteAProduct(String id) {
        // Kiểm tra product có tồn tại không trước khi xóa
        ProductEntity existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", id));

        existingProduct.setIsDeleted(true);
        ProductEntity saved = productRepository.save(existingProduct);

        return productMapper.toDTO(saved);
    }

    @Override
    public void deleteMultipleProducts(List<String> ids) {
        ids.forEach(this::deleteAProduct);
    }

    private String safeString(Object value) {
        return value == null ? "" : value.toString();
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
