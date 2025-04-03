package com.fixit.FixIt.service;

import com.fixit.FixIt.dto.AdminProfileResponse;
import com.fixit.FixIt.model.Issue;
import com.fixit.FixIt.model.IssueStatus;

import java.util.Map;

public interface AdminService {
    
    /**
     * Get admin profile with stats
     * @param uid Admin UID
     * @return AdminProfileResponse containing profile and stats information
     */
    AdminProfileResponse getAdminProfile(String uid);
    
    /**
     * Get dashboard stats for an admin
     * @param uid Admin UID
     * @return Map containing dashboard metrics
     */
    Map<String, Object> getAdminDashboardStats(String uid);
    
    /**
     * Update issue status by admin
     * @param issueId Issue ID
     * @param newStatus New status
     * @param adminUid Admin UID
     * @param notes Optional notes for the status change
     * @return Updated Issue
     */
    Issue updateIssueStatus(String issueId, IssueStatus newStatus, String adminUid, String notes);
    
    /**
     * Get status change history for an issue
     * @param issueId Issue ID
     * @return Map containing history data
     */
    Map<String, Object> getIssueStatusHistory(String issueId);
    
    /**
     * Get analytics data
     * @param startDate Start date in ISO format (yyyy-MM-dd)
     * @param endDate End date in ISO format (yyyy-MM-dd)
     * @return Map containing analytics data
     */
    Map<String, Object> getAnalytics(String startDate, String endDate);
} 