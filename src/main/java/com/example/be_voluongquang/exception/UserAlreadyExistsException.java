package com.example.be_voluongquang.exception;

/**
 * Exception được throw khi user đã tồn tại
 */
public class UserAlreadyExistsException extends RuntimeException {
    
    public UserAlreadyExistsException(String email) {
        super("User with email '" + email + "' already exists");
    }
    
    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
} 