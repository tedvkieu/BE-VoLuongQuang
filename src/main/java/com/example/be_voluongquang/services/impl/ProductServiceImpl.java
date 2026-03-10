package com.example.be_voluongquang.services.impl;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
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
import com.example.be_voluongquang.dto.request.product.ProductVariantRequestDTO;
import com.example.be_voluongquang.dto.response.product.ProductResponseDTO;
import com.example.be_voluongquang.dto.response.product.ProductVariantResponseDTO;
import com.example.be_voluongquang.entity.BrandEntity;
import com.example.be_voluongquang.entity.CategoryEntity;
import com.example.be_voluongquang.entity.ProductEntity;
import com.example.be_voluongquang.entity.ProductGroupEntity;
import com.example.be_voluongquang.entity.FileArchivalEntity;
import com.example.be_voluongquang.entity.ProductVariantEntity;
import com.example.be_voluongquang.exception.ProductAlreadyExistsException;
import com.example.be_voluongquang.exception.ResourceNotFoundException;
import com.example.be_voluongquang.mapper.ProductMapper;
import com.example.be_voluongquang.repository.BrandRepository;
import com.example.be_voluongquang.repository.CategoryRepository;
import com.example.be_voluongquang.repository.CartItemRepository;
import com.example.be_voluongquang.repository.FileArchivalRepository;
import com.example.be_voluongquang.repository.ProductGroupRepository;
import com.example.be_voluongquang.repository.ProductRepository;
import com.example.be_voluongquang.repository.ProductVariantRepository;
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
import java.util.HashSet;
import java.util.Set;
import java.util.Comparator;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageImpl;

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
    private final ProductVariantRepository productVariantRepository;
    private final CartItemRepository cartItemRepository;

    public ProductServiceImpl(ProductRepository productRepository,
            CategoryRepository categoryRepository,
            BrandRepository brandRepository,
            ProductGroupRepository productGroupRepository,
            UploadImgImgService uploadImgImgService,
            FileArchivalRepository fileArchivalRepository,
            ImageNamingUtil imageNamingUtil,
            ProductVariantRepository productVariantRepository,
            CartItemRepository cartItemRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.productGroupRepository = productGroupRepository;
        this.uploadImgImgService = uploadImgImgService;
        this.fileArchivalRepository = fileArchivalRepository;
        this.imageNamingUtil = imageNamingUtil;
        this.productVariantRepository = productVariantRepository;
        this.cartItemRepository = cartItemRepository;
    }

    // Service Impl for GET Method -----------------------------------------

    @Override
    public List<ProductResponseDTO> getAllProduct() {
        return productMapper.toDtoList(productRepository.findByIsDeletedFalse());
    }

    @Override
    public ProductResponseDTO getProductById(String id) {
        ProductEntity product = productRepository.findProductWithDetailsAndProductVariants(id)
                .orElseGet(() -> productRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", id)));
        if (Boolean.TRUE.equals(product.getIsDeleted())) {
            throw new ResourceNotFoundException("Product", "productId", id);
        }
        return productMapper.toDTO(product);
    }

    @Override
    public List<ProductResponseDTO> getFeaturedProducts() {
        return productMapper.toDtoList(productRepository.findAllByIsFeaturedAndIsDeletedFalse(true));
    }

    @Override
    public List<ProductResponseDTO> getAllProductsDiscount() {
        return productMapper.toDtoList(
                productRepository.findTop4ByIsDeletedFalseAndIsFsaleTrueAndDiscountPercentGreaterThanOrderByDiscountPercentDesc(
                        0));
    }

    @Override
    public Page<ProductResponseDTO> getProductsPaged(int page, int size, String search) {
        int safePage = Math.max(0, page);
        int safeSize = size <= 0 ? 15 : size;
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "updatedAt"));

        ProductSearchRequest request = new ProductSearchRequest();
        request.setSearch(search);
        request.setIsDeleted(false);
        Specification<ProductEntity> specification = buildProductSpecification(request);
        Page<ProductEntity> entityPage = productRepository.findAll(specification, pageable);

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
            entityPage = productRepository.findByDiscountPercentGreaterThanAndIsDeletedFalse(0, pageable);
        }

        return entityPage.map(productMapper::toDTO);
    }

    @Override
    public Page<ProductResponseDTO> getFlashSaleDiscountProductsPaged(int page, int size, String search) {
        int safePage = Math.max(0, page);
        int safeSize = size <= 0 ? 15 : size;
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "discountPercent"));

        String processedSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        Page<ProductEntity> entityPage = processedSearch != null
                ? productRepository.searchFlashSaleDiscountedProducts(0, processedSearch, pageable)
                : productRepository.findByIsDeletedFalseAndIsFsaleTrueAndDiscountPercentGreaterThan(0, pageable);

        return entityPage.map(productMapper::toDTO);
    }

    @Override
    public Page<ProductResponseDTO> searchProducts(ProductSearchRequest request) {
        int safePage = request.getPage() != null ? Math.max(0, request.getPage()) : 0;
        int safeSize = (request.getSize() != null && request.getSize() > 0) ? request.getSize() : 15;
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.ASC, "productId"));

        Specification<ProductEntity> specification = buildProductSpecification(request);
        Page<ProductEntity> entityPage = productRepository.findAll(specification, pageable);

        List<ProductEntity> entities = entityPage.getContent();
        if (entities.isEmpty()) {
            return entityPage.map(productMapper::toDTO);
        }

        List<String> productIds = entities.stream()
                .map(ProductEntity::getProductId)
                .filter(StringUtils::hasText)
                .toList();

        Map<String, List<ProductVariantResponseDTO>> variantsByProductId = new HashMap<>();
        if (!productIds.isEmpty()) {
            productVariantRepository.findActiveByProductIds(productIds).stream()
                    .filter(v -> v != null && v.getProduct() != null && StringUtils.hasText(v.getProduct().getProductId()))
                    .collect(Collectors.groupingBy(v -> v.getProduct().getProductId()))
                    .forEach((pid, variants) -> {
                        List<ProductVariantResponseDTO> mapped = variants.stream()
                                .sorted(Comparator.comparing(
                                        (ProductVariantEntity v) -> v.getSortOrder() == null ? Integer.MAX_VALUE : v.getSortOrder()))
                                .map(v -> ProductVariantResponseDTO.builder()
                                        .productVariantId(v.getProductVariantId())
                                        .variantName(v.getVariantName())
                                        .variantPrice(v.getVariantPrice())
                                        .finalPrice(v.getFinalPrice())
                                        .stockQuantity(v.getStockQuantity())
                                        .sortOrder(v.getSortOrder())
                                        .isDeleted(v.getIsDeleted())
                                        .build())
                                .toList();
                        variantsByProductId.put(pid, mapped);
                    });
        }

        List<ProductResponseDTO> dtos = entities.stream()
                .map(entity -> {
                    ProductResponseDTO dto = productMapper.toDTO(entity);
                    if (dto != null && StringUtils.hasText(dto.getProductId())) {
                        List<ProductVariantResponseDTO> variants =
                                variantsByProductId.get(dto.getProductId());
                        if (variants != null && !variants.isEmpty()) {
                            dto.setProductVariants(variants);
                        }
                    }
                    return dto;
                })
                .toList();

        return new PageImpl<>(dtos, pageable, entityPage.getTotalElements());
    }

    @Override
    public byte[] exportProducts(ProductSearchRequest request) {
        ProductSearchRequest safeRequest = (request != null) ? request : new ProductSearchRequest();
        if (safeRequest.getIsDeleted() == null) {
            safeRequest.setIsDeleted(false);
        }
        int safePage = safeRequest.getPage() != null ? Math.max(0, safeRequest.getPage()) : 0;
        int safeSize = (safeRequest.getSize() != null && safeRequest.getSize() > 0) ? safeRequest.getSize() : 15;
        Specification<ProductEntity> specification = buildProductSpecification(safeRequest);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "updatedAt"));
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
            Boolean deletedFilter =
                    request != null && request.getIsDeleted() != null ? request.getIsDeleted() : Boolean.FALSE;
            predicates.add(cb.equal(root.get("isDeleted"), deletedFilter));

            if (StringUtils.hasText(request.getSearch())) {
                String normalized = request.getSearch().trim().toLowerCase(Locale.ROOT);
                String term = "%" + normalized + "%";
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
                predicates.add(cb.greaterThanOrEqualTo(discountedPriceExpression(root, cb), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(discountedPriceExpression(root, cb), maxPrice));
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

    private jakarta.persistence.criteria.Expression<Double> discountedPriceExpression(
            jakarta.persistence.criteria.Root<ProductEntity> root,
            jakarta.persistence.criteria.CriteriaBuilder cb) {
        jakarta.persistence.criteria.Expression<Double> price =
                cb.coalesce(root.get("price"), 0.0);
        jakarta.persistence.criteria.Expression<Integer> discount =
                cb.coalesce(root.get("discountPercent"), 0);
        jakarta.persistence.criteria.Expression<Double> discountAsDouble = cb.toDouble(discount);
        jakarta.persistence.criteria.Expression<Double> numerator = cb.toDouble(
                cb.diff(cb.literal(100.0), discountAsDouble));
        jakarta.persistence.criteria.Expression<Double> factor = cb.toDouble(
                cb.quot(numerator, cb.literal(100.0)));
        return cb.toDouble(cb.prod(price, factor));
    }

    // Service Impl for POST Method -----------------------------------------

    @Override
    @Transactional
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
        product.setIsDeleted(false);
        ProductEntity savedProduct = productRepository.save(product);
        syncFileArchivals(savedProduct, imageUrl);
        syncProductVariants(savedProduct, dto.getProductVariants());

        ProductEntity reloaded = productRepository.findProductWithDetailsAndProductVariants(savedProduct.getProductId())
                .orElse(savedProduct);
        return productMapper.toDTO(reloaded);
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
        updatedProduct.setIsDeleted(existingProduct.getIsDeleted()); // Giữ nguyên trạng thái xoá
        updatedProduct.setIsFsale(existingProduct.getIsFsale()); // Giữ nguyên trạng thái flash sale
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
        syncProductVariants(savedProduct, dto.getProductVariants());

        ProductEntity reloaded = productRepository.findProductWithDetailsAndProductVariants(savedProduct.getProductId())
                .orElse(savedProduct);
        return productMapper.toDTO(reloaded);
    }

    private void syncProductVariants(ProductEntity product, List<ProductVariantRequestDTO> payload) {
        if (product == null || !StringUtils.hasText(product.getProductId())) {
            return;
        }

        String productId = product.getProductId();
        if (payload == null) {
            // Client didn't send productVariants -> do not mutate existing variants.
            return;
        }

        List<ProductVariantEntity> existing =
                productVariantRepository.findByProductProductIdAndIsDeletedFalseOrderBySortOrderAsc(productId);
        Map<String, ProductVariantEntity> existingById = new HashMap<>();
        for (ProductVariantEntity v : existing) {
            if (v != null && StringUtils.hasText(v.getProductVariantId())) {
                existingById.put(v.getProductVariantId(), v);
            }
        }

        if (payload.isEmpty()) {
            // Explicitly clear variants
            if (!existing.isEmpty()) {
                List<String> removedIds = existing.stream()
                        .map(ProductVariantEntity::getProductVariantId)
                        .filter(StringUtils::hasText)
                        .toList();
                if (!removedIds.isEmpty()) {
                    cartItemRepository.deleteByProductVariantIds(removedIds);
                }
                for (ProductVariantEntity v : existing) {
                    if (v != null) v.setIsDeleted(true);
                }
                productVariantRepository.saveAll(existing);
            }
            return;
        }

        Set<String> incomingIds = new HashSet<>();
        List<ProductVariantEntity> toSave = new ArrayList<>();
        int fallbackOrder = 0;
        for (ProductVariantRequestDTO item : payload) {
            if (item == null) {
                continue;
            }
            String rawId = item.getProductVariantId();
            String variantId = StringUtils.hasText(rawId) ? rawId.trim() : null;

            String variantName = item.getVariantName() == null ? "" : item.getVariantName().trim();
            if (!StringUtils.hasText(variantName)) {
                // Allow skipping "new empty row" but never allow blanking an existing variant.
                if (variantId != null) {
                    throw new IllegalArgumentException("Tên phân loại không hợp lệ");
                }
                continue;
            }
            Double variantPrice = item.getVariantPrice();
            if (variantPrice == null || !Double.isFinite(variantPrice) || variantPrice < 0) {
                if (variantId != null) {
                    throw new IllegalArgumentException("Giá phân loại không hợp lệ");
                }
                continue;
            }

            Integer stockQuantity = item.getStockQuantity();
            if (stockQuantity == null) {
                stockQuantity = 0;
            }
            if (stockQuantity < 0) {
                stockQuantity = 0;
            }

            int discountPercent = product.getDiscountPercent() == null ? 0 : product.getDiscountPercent();
            if (discountPercent < 0) discountPercent = 0;
            if (discountPercent > 100) discountPercent = 100;
            double finalPrice = variantPrice * (1 - discountPercent / 100.0);

            Integer sortOrder = item.getSortOrder();
            if (sortOrder == null) {
                sortOrder = fallbackOrder;
            }
            fallbackOrder++;

            if (variantId != null) {
                if (!incomingIds.add(variantId)) {
                    throw new IllegalArgumentException("Danh sách phân loại bị trùng (productVariantId)");
                }

                ProductVariantEntity existingVariant = existingById.get(variantId);
                if (existingVariant == null) {
                    ProductVariantEntity found = productVariantRepository.findById(variantId)
                            .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "productVariantId", variantId));
                    String ownerProductId =
                            found.getProduct() != null ? found.getProduct().getProductId() : null;
                    if (ownerProductId == null || !ownerProductId.equals(productId)) {
                        throw new IllegalArgumentException("Phân loại sản phẩm không thuộc sản phẩm đã chọn");
                    }
                    existingVariant = found;
                }

                existingVariant.setProduct(product);
                existingVariant.setVariantName(variantName);
                existingVariant.setVariantPrice(variantPrice);
                existingVariant.setFinalPrice(finalPrice);
                existingVariant.setStockQuantity(stockQuantity);
                existingVariant.setSortOrder(sortOrder);
                existingVariant.setIsDeleted(false);
                toSave.add(existingVariant);
            } else {
                toSave.add(ProductVariantEntity.builder()
                        .product(product)
                        .variantName(variantName)
                        .variantPrice(variantPrice)
                        .finalPrice(finalPrice)
                        .stockQuantity(stockQuantity)
                        .sortOrder(sortOrder)
                        .build());
            }
        }

        if (!toSave.isEmpty()) {
            productVariantRepository.saveAll(toSave);
        }

        // Soft-delete variants removed from payload; also remove them from carts to avoid stale/unbuyable items.
        if (!existing.isEmpty()) {
            List<ProductVariantEntity> removed = new ArrayList<>();
            List<String> removedIds = new ArrayList<>();
            for (ProductVariantEntity v : existing) {
                if (v == null) continue;
                String existingId = v.getProductVariantId();
                if (!StringUtils.hasText(existingId)) continue;
                if (!incomingIds.contains(existingId)) {
                    v.setIsDeleted(true);
                    removed.add(v);
                    removedIds.add(existingId);
                }
            }
            if (!removedIds.isEmpty()) {
                cartItemRepository.deleteByProductVariantIds(removedIds);
                productVariantRepository.saveAll(removed);
            }
        }
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
        productVariantRepository.recomputeFinalPriceForProduct(id, sanitized);
        return productMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public ProductResponseDTO updateFsale(String id, boolean isFsale) {
        ProductEntity existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", id));
        existingProduct.setIsFsale(isFsale);
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
        ProductEntity existingProduct = productRepository.findByIdIncludingDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", id));

        if (Boolean.TRUE.equals(existingProduct.getIsDeleted())) {
            return productMapper.toDTO(existingProduct);
        }

        int updated = productRepository.softDeleteById(id);
        if (updated == 0 && !productRepository.existsAnyById(id)) {
            throw new ResourceNotFoundException("Product", "productId", id);
        }

        existingProduct.setIsDeleted(true);
        return productMapper.toDTO(existingProduct);
    }

    @Override
    @Transactional
    public ProductResponseDTO restoreProduct(String id) {
        ProductEntity existingProduct = productRepository.findByIdIncludingDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", id));

        if (Boolean.FALSE.equals(existingProduct.getIsDeleted())) {
            return productMapper.toDTO(existingProduct);
        }

        existingProduct.setIsDeleted(false);
        int updated = productRepository.softRestoreById(id);
        if (updated == 0) {
            // Fallback if native query fails for some reason, though entity save should handle it
            existingProduct.setIsDeleted(false);
            productRepository.save(existingProduct);
        }
        
        return productMapper.toDTO(existingProduct);
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
