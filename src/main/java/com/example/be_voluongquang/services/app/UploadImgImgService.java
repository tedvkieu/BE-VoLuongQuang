package com.example.be_voluongquang.services.app;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import com.example.be_voluongquang.exception.FileUploadException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class UploadImgImgService {
    private final S3Client s3Client;

    @Value("${cloudflare.r2.bucket}")
    private String bucketName;

    @Value("${cloudflare.r2.endpoint}")
    private String endpoint;

    @Value("${cloudflare.r2.public-base-url}")
    private String publicBaseUrl;

    public String handleSaveUploadFile(MultipartFile file, String targetFolder) {
        if (file.isEmpty()) {
            return "";
        }
        if (!StringUtils.hasText(bucketName)) {
            throw new FileUploadException("R2 bucket is missing. Please set cloudflare.r2.bucket.");
        }

        String originalName = file.getOriginalFilename();
        String safeName = StringUtils.hasText(originalName) ? originalName.trim() : "upload";
        String uniquePrefix = Instant.now().toEpochMilli() + "-" + UUID.randomUUID();
        String finalName = uniquePrefix + "-" + safeName;

        String folder = StringUtils.hasText(targetFolder) ? targetFolder.trim() : "";
        String objectKey = folder.isEmpty() ? finalName : folder + "/" + finalName;

        try {
            byte[] bytes = file.getBytes();
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(bytes));
            return buildPublicUrl(objectKey);
        } catch (IOException e) {
            throw new FileUploadException("Failed to upload file: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new FileUploadException("Failed to upload file to R2: " + e.getMessage(), e);
        }
    }

    private String buildPublicUrl(String objectKey) {
        String base = StringUtils.hasText(publicBaseUrl) ? publicBaseUrl : endpoint;
        if (!StringUtils.hasText(base)) {
            // last resort: return key only
            return objectKey;
        }

        String cleanEndpoint = StringUtils.trimTrailingCharacter(
                StringUtils.trimWhitespace(base), '/');
        String cleanKey = StringUtils.trimLeadingCharacter(objectKey, '/');
        return cleanEndpoint + "/" + cleanKey;
    }
}
