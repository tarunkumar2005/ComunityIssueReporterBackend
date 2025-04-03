package com.fixit.FixIt.dto;

import jakarta.validation.constraints.NotBlank;

public class UploadImageRequest {
    @NotBlank(message = "Image URL is required")
    private String imageUrl;
    
    private String issueId;
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getIssueId() {
        return issueId;
    }
    
    public void setIssueId(String issueId) {
        this.issueId = issueId;
    }
} 