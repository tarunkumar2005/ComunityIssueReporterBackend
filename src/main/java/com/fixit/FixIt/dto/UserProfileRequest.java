package com.fixit.FixIt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Map;

public class UserProfileRequest {
    
    @Size(min = 1, max = 50, message = "Name must be between 1 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9 .'\\-_]+$", message = "Name can only contain letters, numbers, spaces, and these special characters: . ' - _")
    private String name;
    
    private Map<String, Boolean> notificationPreferences;

    // Default constructor
    public UserProfileRequest() {
    }

    // Getters and setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Boolean> getNotificationPreferences() {
        return notificationPreferences;
    }

    public void setNotificationPreferences(Map<String, Boolean> notificationPreferences) {
        this.notificationPreferences = notificationPreferences;
    }
    
    @Override
    public String toString() {
        return "UserProfileRequest{" +
                "name='" + name + '\'' +
                ", notificationPreferences=" + notificationPreferences +
                '}';
    }
}
