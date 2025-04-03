package com.fixit.FixIt.dto;

import com.fixit.FixIt.model.IssueStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateIssueStatusRequest {
    @NotNull(message = "Status is required")
    private IssueStatus status;
    
    private String notes;
    
    // Getters and Setters
    public IssueStatus getStatus() {
        return status;
    }
    
    public void setStatus(IssueStatus status) {
        this.status = status;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
} 