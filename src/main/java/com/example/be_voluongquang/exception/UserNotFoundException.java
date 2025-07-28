package com.example.be_voluongquang.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String id) {
        super("User with ID: '" + id + "' Not Found");
    }

    // public UserNotFoundException(String email) {
    // super("User with email '" + email + "' not found");
    // }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
