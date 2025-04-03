package com.fixit.FixIt.dto;

import jakarta.validation.constraints.NotBlank;

public class UpvoteIssueRequest {
    @NotBlank(message = "User ID is required")
    private String userId;

    // Default constructor
    public UpvoteIssueRequest() {}

    public UpvoteIssueRequest(String userId) {
        this.userId = userId;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
} 