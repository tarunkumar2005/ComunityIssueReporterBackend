package com.fixit.FixIt.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SuccessResponse<T> {
    private LocalDateTime timestamp;
    private String message;
    private String status;
    private T data;

    public SuccessResponse() {
        this.timestamp = LocalDateTime.now();
        this.status = "success";
    }

    public SuccessResponse(String message) {
        this();
        this.message = message;
    }

    public SuccessResponse(String message, T data) {
        this(message);
        this.data = data;
    }

    public static <T> SuccessResponse<T> of(String message, T data) {
        return new SuccessResponse<>(message, data);
    }

    public static <T> SuccessResponse<T> of(T data) {
        SuccessResponse<T> response = new SuccessResponse<>();
        response.setData(data);
        return response;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
} 