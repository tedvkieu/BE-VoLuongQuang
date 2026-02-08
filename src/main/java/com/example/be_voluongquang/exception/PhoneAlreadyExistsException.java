package com.example.be_voluongquang.exception;

/**
 * Exception được throw khi số điện thoại đã tồn tại
 */
public class PhoneAlreadyExistsException extends RuntimeException {
    public PhoneAlreadyExistsException(String phone) {
        super("Số điện thoại '" + phone + "' đã tồn tại trong hệ thống");
    }
}

