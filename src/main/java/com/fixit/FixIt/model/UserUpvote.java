package com.fixit.FixIt.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UserUpvote {
    private String id;
    private String userId;
    private String issueId;
    private Date createdAt;

    // Default constructor
    public UserUpvote() {
        this.createdAt = new Date();
    }

    public UserUpvote(String userId, String issueId) {
        this.userId = userId;
        this.issueId = issueId;
        this.createdAt = new Date();
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getIssueId() {
        return issueId;
    }

    public void setIssueId(String issueId) {
        this.issueId = issueId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    // Convert to Map for Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("userId", userId);
        map.put("issueId", issueId);
        map.put("createdAt", createdAt);
        return map;
    }

    // Create from Firestore document
    public static UserUpvote fromMap(Map<String, Object> map) {
        UserUpvote userUpvote = new UserUpvote();
        userUpvote.setId((String) map.get("id"));
        userUpvote.setUserId((String) map.get("userId"));
        userUpvote.setIssueId((String) map.get("issueId"));
        userUpvote.setCreatedAt((Date) map.get("createdAt"));
        return userUpvote;
    }
} 