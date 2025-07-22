package com.example.be_voluongquang.utils;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImageNamingUtil {

    private static final Map<String, String> CATEGORY_FOLDER_MAP = new HashMap<>();

    static {
        CATEGORY_FOLDER_MAP.put("CAT0001", "dry_prd");
        CATEGORY_FOLDER_MAP.put("CAT0002", "cool_prd");
        CATEGORY_FOLDER_MAP.put("CAT0003", "spice");
        CATEGORY_FOLDER_MAP.put("CAT0004", "snack");
        CATEGORY_FOLDER_MAP.put("CAT0005", "other");
    }

    public static String getFolderByCategoryId(String categoryId) {
        log.info("=== DEBUG: ImageNamingUtil.getFolderByCategoryId ===");
        log.info("Category ID: {}", categoryId);
        
        String folder = CATEGORY_FOLDER_MAP.getOrDefault(categoryId, "unknown");
        log.info("Mapped folder: {}", folder);
        
        return folder;
    }

    /**
     * Trả về tên ảnh đầy đủ kèm timestamp và thư mục
     * Ví dụ: dry_prd/1723459829123-image1.png
     */
    public static String buildFinalImagePath(String originalFilename, String categoryId) {
        log.info("=== DEBUG: ImageNamingUtil.buildFinalImagePath ===");
        log.info("Original filename: {}", originalFilename);
        log.info("Category ID: {}", categoryId);
        
        String folder = getFolderByCategoryId(categoryId);
        String finalPath = folder + "/" + originalFilename;
        
        log.info("Final image path: {}", finalPath);
        return finalPath;
    }
}

