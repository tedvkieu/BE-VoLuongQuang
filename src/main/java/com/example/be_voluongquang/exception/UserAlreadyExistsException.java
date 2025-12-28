package com.example.be_voluongquang.exception;

/**
 * Exception được throw khi user đã tồn tại
 */
public class UserAlreadyExistsException extends RuntimeException {
    
    public UserAlreadyExistsException(String email) {
        super("Email '" + email + "' đã tồn tại trong hệ thống");
    }
    
    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
} 
