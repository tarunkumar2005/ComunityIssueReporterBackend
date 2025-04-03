package com.fixit.FixIt.dto;

import java.util.HashMap;
import java.util.Map;

public class AdminAuthResponse {
    private String token;
    private String uid;
    private String name;
    private String phoneNumber;
    private String location;
    private String role;
    private Map<String, Boolean> permissions;
    private Map<String, Object> stats;
    
    public AdminAuthResponse() {
        this.permissions = new HashMap<>();
        this.stats = new HashMap<>();
    }
    
    public AdminAuthResponse(String token, String uid, String name, String phoneNumber, String location) {
        this.token = token;
        this.uid = uid;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.location = location;
        this.role = "ADMIN";
        this.permissions = new HashMap<>();
        this.stats = new HashMap<>();
    }
    
    // Getters and Setters
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
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public Map<String, Boolean> getPermissions() {
        return permissions;
    }
    
    public void setPermissions(Map<String, Boolean> permissions) {
        this.permissions = permissions;
    }
    
    public Map<String, Object> getStats() {
        return stats;
    }
    
    public void setStats(Map<String, Object> stats) {
        this.stats = stats;
    }
} 