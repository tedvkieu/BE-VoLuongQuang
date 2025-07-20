package com.example.be_voluongquang.exception;

import com.example.be_voluongquang.dto.response.ErrorResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global Exception Handler để xử lý tất cả exception trong ứng dụng
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Xử lý UserAlreadyExistsException
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserAlreadyExistsException(
            UserAlreadyExistsException ex, 
            HttpServletRequest request) {
        
        log.error("User already exists: {}", ex.getMessage());
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Xử lý InvalidCredentialsException
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidCredentialsException(
            InvalidCredentialsException ex, 
            HttpServletRequest request) {
        
        log.error("Invalid credentials: {}", ex.getMessage());
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Xử lý ProductAlreadyExistsException
     */
    @ExceptionHandler(ProductAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleProductAlreadyExistsException(
            ProductAlreadyExistsException ex, 
            HttpServletRequest request) {
        
        log.error("Product already exists: {}", ex.getMessage());
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Xử lý ResourceNotFoundException
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFoundException(
            ResourceNotFoundException ex, 
            HttpServletRequest request) {
        
        log.error("Resource not found: {}", ex.getMessage());
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Xử lý FileUploadException
     */
    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ErrorResponseDTO> handleFileUploadException(
            FileUploadException ex, 
            HttpServletRequest request) {
        
        log.error("File upload error: {}", ex.getMessage());
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Xử lý validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(
            MethodArgumentNotValidException ex, 
            HttpServletRequest request) {
        
        List<String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        
        log.error("Validation error: {}", details);
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Validation failed",
                request.getRequestURI(),
                details
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Xử lý MaxUploadSizeExceededException
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponseDTO> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex, 
            HttpServletRequest request) {
        
        log.error("File size exceeded: {}", ex.getMessage());
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.PAYLOAD_TOO_LARGE.value(),
                "Payload Too Large",
                "File size exceeds maximum allowed size",
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(errorResponse);
    }

    /**
     * Xử lý DataIntegrityViolationException
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, 
            HttpServletRequest request) {
        
        log.error("Data integrity violation: {}", ex.getMessage());
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                "Data integrity constraint violation",
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Xử lý MethodArgumentTypeMismatchException
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, 
            HttpServletRequest request) {
        
        log.error("Method argument type mismatch: {}", ex.getMessage());
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Parameter type mismatch: " + ex.getName() + " should be of type " + ex.getRequiredType().getSimpleName(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Xử lý HttpMessageNotReadableException
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, 
            HttpServletRequest request) {
        
        log.error("HTTP message not readable: {}", ex.getMessage());
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Invalid request body format",
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Xử lý IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(
            IllegalArgumentException ex, 
            HttpServletRequest request) {
        
        log.error("Illegal argument: {}", ex.getMessage());
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Xử lý tất cả các exception khác (catch-all)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGlobalException(
            Exception ex, 
            HttpServletRequest request) {
        
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred",
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
} 