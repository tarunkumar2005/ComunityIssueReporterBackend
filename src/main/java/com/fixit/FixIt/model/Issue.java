package com.fixit.FixIt.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Issue {
    private String id;
    private String title;
    private String description;
    private String location;
    private Double latitude;
    private Double longitude;
    private String reporterUid;
    private String reporterName;
    private IssueStatus status;
    private Date createdAt;
    private Date updatedAt;
    private Integer upvotes;
    private List<String> imageUrls;
    
    // Admin related fields
    private String handledByAdminUid;
    private String handledByAdminName;
    private Date lastStatusChangeAt;
    private List<StatusChangeLog> statusChangeLogs;
    private String adminNotes;
    
    public Issue() {
        this.status = IssueStatus.OPEN;
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.upvotes = 0;
        this.imageUrls = new ArrayList<>();
        this.statusChangeLogs = new ArrayList<>();
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public Double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    
    public Double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    
    public String getReporterUid() {
        return reporterUid;
    }
    
    public void setReporterUid(String reporterUid) {
        this.reporterUid = reporterUid;
    }
    
    public String getReporterName() {
        return reporterName;
    }
    
    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }
    
    public IssueStatus getStatus() {
        return status;
    }
    
    public void setStatus(IssueStatus status) {
        this.status = status;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Integer getUpvotes() {
        return upvotes;
    }
    
    public void setUpvotes(Integer upvotes) {
        this.upvotes = upvotes;
    }
    
    public List<String> getImageUrls() {
        return imageUrls;
    }
    
    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
    
    public void addImageUrl(String imageUrl) {
        if (this.imageUrls == null) {
            this.imageUrls = new ArrayList<>();
        }
        this.imageUrls.add(imageUrl);
    }
    
    // Admin related getters and setters
    public String getHandledByAdminUid() {
        return handledByAdminUid;
    }
    
    public void setHandledByAdminUid(String handledByAdminUid) {
        this.handledByAdminUid = handledByAdminUid;
    }
    
    public String getHandledByAdminName() {
        return handledByAdminName;
    }
    
    public void setHandledByAdminName(String handledByAdminName) {
        this.handledByAdminName = handledByAdminName;
    }
    
    public Date getLastStatusChangeAt() {
        return lastStatusChangeAt;
    }
    
    public void setLastStatusChangeAt(Date lastStatusChangeAt) {
        this.lastStatusChangeAt = lastStatusChangeAt;
    }
    
    public List<StatusChangeLog> getStatusChangeLogs() {
        return statusChangeLogs;
    }
    
    public void setStatusChangeLogs(List<StatusChangeLog> statusChangeLogs) {
        this.statusChangeLogs = statusChangeLogs;
    }
    
    public void addStatusChangeLog(StatusChangeLog log) {
        if (this.statusChangeLogs == null) {
            this.statusChangeLogs = new ArrayList<>();
        }
        this.statusChangeLogs.add(log);
    }
    
    public String getAdminNotes() {
        return adminNotes;
    }
    
    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }
} 