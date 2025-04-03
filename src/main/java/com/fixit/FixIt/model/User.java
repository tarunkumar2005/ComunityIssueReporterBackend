package com.fixit.FixIt.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class User {
    private String uid;
    private String username;
    private String email;
    private String name;
    private String displayName;
    private String role;
    private String location;
    private Date accountCreationDate;
    private Date lastLogin;
    private Map<String, Boolean> notificationPreferences;

    // Default constructor
    public User() {
        this.notificationPreferences = new HashMap<>();
        this.notificationPreferences.put("ownIssues", true);
        this.notificationPreferences.put("communityActivity", false);
    }

    public User(String uid, String username, String email) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.role = "USER";
        this.accountCreationDate = new Date();
        this.lastLogin = new Date();
        this.notificationPreferences = new HashMap<>();
        this.notificationPreferences.put("ownIssues", true);
        this.notificationPreferences.put("communityActivity", false);
    }
    
    public User(String uid, String username, String email, String name) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.name = name;
        this.displayName = name; // Default display name to name
        this.role = "USER";
        this.accountCreationDate = new Date();
        this.lastLogin = new Date();
        this.notificationPreferences = new HashMap<>();
        this.notificationPreferences.put("ownIssues", true);
        this.notificationPreferences.put("communityActivity", false);
    }

    // Getters and setters
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getAccountCreationDate() {
        return accountCreationDate;
    }

    public void setAccountCreationDate(Date accountCreationDate) {
        this.accountCreationDate = accountCreationDate;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Map<String, Boolean> getNotificationPreferences() {
        return notificationPreferences;
    }

    public void setNotificationPreferences(Map<String, Boolean> notificationPreferences) {
        this.notificationPreferences = notificationPreferences;
    }

    // Convert to Map for Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("username", username);
        map.put("email", email);
        map.put("name", name);
        map.put("displayName", displayName);
        map.put("role", role);
        map.put("location", location);
        map.put("accountCreationDate", accountCreationDate);
        map.put("lastLogin", lastLogin);
        map.put("notificationPreferences", notificationPreferences);
        return map;
    }

    // Create from Firestore document
    public static User fromMap(Map<String, Object> map) {
        User user = new User();
        user.setUid((String) map.get("uid"));
        user.setUsername((String) map.get("username"));
        user.setEmail((String) map.get("email"));
        user.setName((String) map.get("name"));
        user.setDisplayName((String) map.get("displayName"));
        user.setRole((String) map.get("role"));
        user.setLocation((String) map.get("location"));
        
        // Handle date conversions
        if (map.get("accountCreationDate") instanceof Date) {
            user.setAccountCreationDate((Date) map.get("accountCreationDate"));
        } else if (map.get("accountCreationDate") instanceof Long) {
            user.setAccountCreationDate(new Date((Long) map.get("accountCreationDate")));
        }
        
        if (map.get("lastLogin") instanceof Date) {
            user.setLastLogin((Date) map.get("lastLogin"));
        } else if (map.get("lastLogin") instanceof Long) {
            user.setLastLogin(new Date((Long) map.get("lastLogin")));
        }
        
        // Handle notification preferences
        @SuppressWarnings("unchecked")
        Map<String, Boolean> notificationPrefs = (Map<String, Boolean>) map.get("notificationPreferences");
        if (notificationPrefs != null) {
            user.setNotificationPreferences(notificationPrefs);
        } else {
            Map<String, Boolean> defaultPrefs = new HashMap<>();
            defaultPrefs.put("ownIssues", true);
            defaultPrefs.put("communityActivity", false);
            user.setNotificationPreferences(defaultPrefs);
        }
        
        return user;
    }
}