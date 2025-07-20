package com.example.be_voluongquang.exception;

/**
 * Exception được throw khi credentials không hợp lệ
 */
public class InvalidCredentialsException extends RuntimeException {
    
    public InvalidCredentialsException(String message) {
        super(message);
    }
    
    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
} 