package com.fixit.FixIt.util;

import com.fixit.FixIt.dto.SuccessResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ApiResponseUtil {

    private ApiResponseUtil() {
        // Private constructor to prevent instantiation
    }

    public static <T> ResponseEntity<SuccessResponse<T>> success(T data) {
        return ResponseEntity.ok(SuccessResponse.of(data));
    }

    public static <T> ResponseEntity<SuccessResponse<T>> success(String message, T data) {
        return ResponseEntity.ok(SuccessResponse.of(message, data));
    }

    public static <T> ResponseEntity<SuccessResponse<T>> success(HttpStatus status, String message, T data) {
        return ResponseEntity.status(status).body(SuccessResponse.of(message, data));
    }

    public static ResponseEntity<SuccessResponse<Void>> success(String message) {
        return ResponseEntity.ok(new SuccessResponse<>(message));
    }

    public static ResponseEntity<SuccessResponse<Void>> created(String message) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponse<>(message));
    }

    public static <T> ResponseEntity<SuccessResponse<T>> created(String message, T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(SuccessResponse.of(message, data));
    }

    public static ResponseEntity<SuccessResponse<Void>> noContent() {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
} 