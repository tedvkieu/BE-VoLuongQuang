package com.example.be_voluongquang.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO chuẩn cho response lỗi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDTO {
    
    /**
     * Timestamp khi lỗi xảy ra
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    /**
     * HTTP status code
     */
    private int status;
    
    /**
     * HTTP status message
     */
    private String error;
    
    /**
     * Message lỗi
     */
    private String message;
    
    /**
     * Path của request gây lỗi
     */
    private String path;
    
    /**
     * Chi tiết lỗi (optional)
     */
    private List<String> details;
    
    /**
     * Constructor đơn giản
     */
    public ErrorResponseDTO(int status, String error, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }
    
    /**
     * Constructor với details
     */
    public ErrorResponseDTO(int status, String error, String message, String path, List<String> details) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.details = details;
    }
} 