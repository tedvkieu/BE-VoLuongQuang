package com.example.be_voluongquang.services.app;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import jakarta.servlet.ServletContext;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.be_voluongquang.exception.FileUploadException;

@Service
public class UploadImgImgService {
    private final ServletContext servletContext;

    public UploadImgImgService(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public String handleSaveUploadFile(MultipartFile file, String targetFolder) {
        if (file.isEmpty()) {
            return "";
        }
        // String rootPath = this.servletContext.getRealPath("/resources/images");
        String rootPath = System.getProperty("user.dir") + "/uploads";

        String finalName = "";
        try {
            byte[] bytes;
            bytes = file.getBytes();

            File dir = new File(rootPath + File.separator + targetFolder);
            if (!dir.exists())
                dir.mkdirs();

            finalName = System.currentTimeMillis() + "-" + file.getOriginalFilename();
            File serverFile = new File(dir.getAbsolutePath() + File.separator + finalName);
            BufferedOutputStream stream = new BufferedOutputStream(
                    new FileOutputStream(serverFile));
            stream.write(bytes);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new FileUploadException("Failed to save file: " + e.getMessage(), e);
        }
        return finalName;
    }
}
