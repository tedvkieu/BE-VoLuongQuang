package com.example.be_voluongquang.exception;

/**
 * Exception được throw khi có lỗi upload file
 */
public class FileUploadException extends RuntimeException {
    
    public FileUploadException(String message) {
        super(message);
    }
    
    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
} 