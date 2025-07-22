package com.example.be_voluongquang.exception;

/**
 * Exception được throw khi product đã tồn tại
 */
public class ProductAlreadyExistsException extends RuntimeException {
    
    public ProductAlreadyExistsException(String productId) {
        super("Product with ID '" + productId + "' already exists");
    }
    
    public ProductAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
} 