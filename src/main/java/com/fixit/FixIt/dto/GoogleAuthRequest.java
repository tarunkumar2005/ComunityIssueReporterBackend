package com.fixit.FixIt.dto;

import jakarta.validation.constraints.NotBlank;

public class GoogleAuthRequest {
    @NotBlank(message = "Google ID token is required")
    private String idToken;
    
    public GoogleAuthRequest() {}
    
    public GoogleAuthRequest(String idToken) {
        this.idToken = idToken;
    }
    
    public String getIdToken() {
        return idToken;
    }
    
    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
} 