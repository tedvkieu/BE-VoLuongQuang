package com.example.be_voluongquang.utils;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.example.be_voluongquang.entity.CategoryEntity;
import com.example.be_voluongquang.repository.CategoryRepository;

@Component
public class ImageNamingUtil {

    private final CategoryRepository categoryRepository;

    public ImageNamingUtil(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public String getFolderByCategoryId(String categoryId) {
        if (!StringUtils.hasText(categoryId)) {
            return "unknown";
        }
        return categoryRepository.findById(categoryId.trim())
                .map(CategoryEntity::getCategoryCode)
                .filter(StringUtils::hasText)
                .orElse("unknown");
    }

    /**
     * Trả về tên ảnh đầy đủ kèm timestamp và thư mục
     * Ví dụ: dry_prd/1723459829123-image1.png
     */
    public String buildFinalImagePath(String originalFilename, String categoryId) {
        String folder = getFolderByCategoryId(categoryId);
        return folder + "/" + originalFilename;
    }
}
