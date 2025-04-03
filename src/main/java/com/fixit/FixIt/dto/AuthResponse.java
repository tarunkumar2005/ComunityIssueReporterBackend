package com.fixit.FixIt.dto;

public class AuthResponse {
    private String token;
    private String uid;
    private String username;

    public AuthResponse() {}

    public AuthResponse(String token, String uid) {
        this.token = token;
        this.uid = uid;
    }

    public AuthResponse(String token, String uid, String username) {
        this.token = token;
        this.uid = uid;
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
} 