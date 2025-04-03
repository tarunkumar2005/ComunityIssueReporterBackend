package com.fixit.FixIt.model;

import java.util.Date;

public class StatusChangeLog {
    private String id;
    private String issueId;
    private IssueStatus fromStatus;
    private IssueStatus toStatus;
    private String changedByAdminUid;
    private String changedByAdminName;
    private Date changedAt;
    private String notes;
    
    public StatusChangeLog() {
        this.changedAt = new Date();
    }
    
    public StatusChangeLog(String issueId, IssueStatus fromStatus, IssueStatus toStatus, 
                           String changedByAdminUid, String changedByAdminName, String notes) {
        this.issueId = issueId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedByAdminUid = changedByAdminUid;
        this.changedByAdminName = changedByAdminName;
        this.notes = notes;
        this.changedAt = new Date();
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getIssueId() {
        return issueId;
    }
    
    public void setIssueId(String issueId) {
        this.issueId = issueId;
    }
    
    public IssueStatus getFromStatus() {
        return fromStatus;
    }
    
    public void setFromStatus(IssueStatus fromStatus) {
        this.fromStatus = fromStatus;
    }
    
    public IssueStatus getToStatus() {
        return toStatus;
    }
    
    public void setToStatus(IssueStatus toStatus) {
        this.toStatus = toStatus;
    }
    
    public String getChangedByAdminUid() {
        return changedByAdminUid;
    }
    
    public void setChangedByAdminUid(String changedByAdminUid) {
        this.changedByAdminUid = changedByAdminUid;
    }
    
    public String getChangedByAdminName() {
        return changedByAdminName;
    }
    
    public void setChangedByAdminName(String changedByAdminName) {
        this.changedByAdminName = changedByAdminName;
    }
    
    public Date getChangedAt() {
        return changedAt;
    }
    
    public void setChangedAt(Date changedAt) {
        this.changedAt = changedAt;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
} 