package com.example.be_voluongquang.services.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
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
    public List<ProductResponseDTO> getFeaturedProducts() {
        return productMapper.toDtoList(productRepository.findAllByIsFeatured(true));
    }

    @Override
    public List<ProductResponseDTO> getAllProductsDiscount() {
        return productMapper.toDtoList(productRepository.findTop4ByOrderByDiscountPercentDesc());
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
    public ProductResponseDTO updateAProduct(String id, MultipartFile[] images, ProductRequestDTO dto) {

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
                    .orElseThrow(() -> new ResourceNotFoundException("Product Group", "productGroupId",
                            dto.getProductGroupId()));
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
            product.setDiscountPercent(CsvParserUtils.parseInteger(row[8]));
            product.setStockQuantity(CsvParserUtils.parseInteger(row[9]));
            product.setWeight(CsvParserUtils.parseDouble(row[10]));
            product.setUnit(row[11]);
            product.setIsFeatured(CsvParserUtils.parseBoolean(row[12]));
            product.setIsActive(CsvParserUtils.parseBoolean(row[13]));
            product.setImageUrl(row[14]);
            product.setDescription(row[15]);
            product.setDescription(row[16]);
            // Parse create_at và update_at nếu entity có các trường này
            if (row.length > 17 && row[17] != null && !row[17].isEmpty()) {
                product.setCreatedAt(LocalDateTime.parse(row[17]));
            }
            if (row.length > 18 && row[18] != null && !row[18].isEmpty()) {
                product.setUpdatedAt(LocalDateTime.parse(row[18]));
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
