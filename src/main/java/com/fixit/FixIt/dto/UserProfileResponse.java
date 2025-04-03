package com.fixit.FixIt.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Date;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileResponse {
    private String uid;
    private String name;
    private String username;
    private String email;
    private Date joinedDate;
    private int issuesReported;
    private int issuesResolved;
    private Map<String, Boolean> notificationPreferences;

    public UserProfileResponse() {
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

    public Date getJoinedDate() {
        return joinedDate;
    }

    public void setJoinedDate(Date joinedDate) {
        this.joinedDate = joinedDate;
    }

    public int getIssuesReported() {
        return issuesReported;
    }

    public void setIssuesReported(int issuesReported) {
        this.issuesReported = issuesReported;
    }

    public int getIssuesResolved() {
        return issuesResolved;
    }

    public void setIssuesResolved(int issuesResolved) {
        this.issuesResolved = issuesResolved;
    }
    
    /**
     * Gets the percentage of reported issues that have been resolved
     * @return percentage as an integer between 0 and 100, or 0 if no issues reported
     */
    public int getResolvedPercentage() {
        if (issuesReported == 0) {
            return 0;
        }
        return (int) Math.round((double) issuesResolved / issuesReported * 100);
    }

    public Map<String, Boolean> getNotificationPreferences() {
        return notificationPreferences;
    }

    public void setNotificationPreferences(Map<String, Boolean> notificationPreferences) {
        this.notificationPreferences = notificationPreferences;
    }
    
    @Override
    public String toString() {
        return "UserProfileResponse{" +
                "uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", joinedDate=" + joinedDate +
                ", issuesReported=" + issuesReported +
                ", issuesResolved=" + issuesResolved +
                ", resolvedPercentage=" + getResolvedPercentage() + "%" +
                ", notificationPreferences=" + notificationPreferences +
                '}';
    }
} 