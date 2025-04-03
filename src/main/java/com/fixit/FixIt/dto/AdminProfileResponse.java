package com.fixit.FixIt.dto;

import java.util.Date;
import java.util.Map;

public class AdminProfileResponse {
    private String uid;
    private String name;
    private String email;
    private String phoneNumber;
    private String location;
    private Date createdAt;
    private Date lastLogin;
    private String role;
    private Map<String, Boolean> permissions;
    private Map<String, Object> stats;
    private Map<String, Object> performanceMetrics;
    private Map<String, Object> timeMetrics;
    private Map<String, Object> categoryStats;
    private Map<String, Object> locationStats;
    
    public AdminProfileResponse() {
    }
    
    // Getters and Setters
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
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
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
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
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
    
    public Map<String, Object> getPerformanceMetrics() {
        return performanceMetrics;
    }
    
    public void setPerformanceMetrics(Map<String, Object> performanceMetrics) {
        this.performanceMetrics = performanceMetrics;
    }
    
    public Map<String, Object> getTimeMetrics() {
        return timeMetrics;
    }
    
    public void setTimeMetrics(Map<String, Object> timeMetrics) {
        this.timeMetrics = timeMetrics;
    }
    
    public Map<String, Object> getCategoryStats() {
        return categoryStats;
    }
    
    public void setCategoryStats(Map<String, Object> categoryStats) {
        this.categoryStats = categoryStats;
    }
    
    public Map<String, Object> getLocationStats() {
        return locationStats;
    }
    
    public void setLocationStats(Map<String, Object> locationStats) {
        this.locationStats = locationStats;
    }
} 