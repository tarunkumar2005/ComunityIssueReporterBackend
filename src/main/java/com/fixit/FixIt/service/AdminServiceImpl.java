package com.fixit.FixIt.service;

import com.fixit.FixIt.dto.AdminProfileResponse;
import com.fixit.FixIt.model.Admin;
import com.fixit.FixIt.model.Issue;
import com.fixit.FixIt.model.IssueStatus;
import com.fixit.FixIt.model.StatusChangeLog;
import com.fixit.FixIt.repository.AdminRepository;
import com.fixit.FixIt.repository.StatusChangeLogRepository;
import com.fixit.FixIt.exception.ResourceNotFoundException;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminServiceImpl.class);
    private final AdminRepository adminRepository;
    private final StatusChangeLogRepository statusChangeLogRepository;
    private final IssueService issueService;

    public AdminServiceImpl(
        AdminRepository adminRepository, 
        StatusChangeLogRepository statusChangeLogRepository,
        IssueService issueService) {
        this.adminRepository = adminRepository;
        this.statusChangeLogRepository = statusChangeLogRepository;
        this.issueService = issueService;
    }

    @Override
    public AdminProfileResponse getAdminProfile(String uid) {
        logger.info("Getting profile for admin: {}", uid);
        
        Admin admin = adminRepository.findByUid(uid)
            .orElseThrow(() -> new ResourceNotFoundException("Admin not found with uid: " + uid));
        
        AdminProfileResponse response = new AdminProfileResponse();
        response.setUid(admin.getUid());
        response.setName(admin.getName());
        response.setEmail(admin.getEmail());
        response.setPhoneNumber(admin.getPhoneNumber());
        response.setLocation(admin.getLocation());
        response.setCreatedAt(admin.getCreatedAt());
        response.setLastLogin(admin.getLastLogin());
        response.setRole(admin.getRole());
        response.setPermissions(admin.getPermissions());
        response.setStats(admin.getStats());
        response.setPerformanceMetrics(admin.getPerformanceMetrics());
        response.setCategoryStats(admin.getCategoryStats());
        response.setTimeMetrics(admin.getTimeMetrics());
        response.setLocationStats(admin.getLocationStats());
        
        return response;
    }

    @Override
    public Map<String, Object> getAdminDashboardStats(String uid) {
        logger.info("Getting dashboard stats for admin: {}", uid);
        
        // Validate admin exists
        Admin admin = adminRepository.findByUid(uid)
            .orElseThrow(() -> new ResourceNotFoundException("Admin not found with uid: " + uid));
        
        Map<String, Object> dashboardStats = new HashMap<>();
        
        // Add all stats from admin object
        dashboardStats.put("adminStats", admin.getStats());
        dashboardStats.put("performanceMetrics", admin.getPerformanceMetrics());
        dashboardStats.put("categoryStats", admin.getCategoryStats());
        dashboardStats.put("timeMetrics", admin.getTimeMetrics());
        dashboardStats.put("locationStats", admin.getLocationStats());
        
        // Calculate completion rate
        int totalIssues = (int) admin.getStats().getOrDefault("totalIssuesHandled", 0);
        int completedIssues = (int) admin.getStats().getOrDefault("issuesResolved", 0) + 
                              (int) admin.getStats().getOrDefault("issuesClosed", 0);
        double completionRate = totalIssues > 0 ? (double) completedIssues / totalIssues * 100.0 : 0.0;
        dashboardStats.put("completionRate", completionRate);
        
        // Add recent issues handled by this admin
        List<StatusChangeLog> recentChanges = statusChangeLogRepository
            .findByChangedByAdminUid(uid)
            .stream()
            .sorted(Comparator.comparing(StatusChangeLog::getChangedAt).reversed())
            .limit(10)
            .collect(Collectors.toList());
        
        dashboardStats.put("recentChanges", recentChanges);
        
        // Get recent activity summary (last 7 days)
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -7);
        Date weekAgo = cal.getTime();
        
        List<StatusChangeLog> recentActivity = statusChangeLogRepository
            .findByChangedByAdminUidAndChangedAtAfter(uid, weekAgo);
        
        Map<String, Integer> activityByStatus = new HashMap<>();
        for (StatusChangeLog log : recentActivity) {
            String statusKey = log.getNewStatus().toString();
            activityByStatus.put(statusKey, activityByStatus.getOrDefault(statusKey, 0) + 1);
        }
        
        dashboardStats.put("recentActivityByStatus", activityByStatus);
        
        // Get workload distribution - e.g., what percentage of all issues the admin has handled
        int adminTotal = totalIssues;
        
        try {
            // Get total issues in the system
            Firestore firestore = FirestoreClient.getFirestore();
            long systemTotal = firestore.collection("issues").get().get().size();
            
            double workloadPercentage = systemTotal > 0 ? (double) adminTotal / systemTotal * 100.0 : 0.0;
            dashboardStats.put("workloadPercentage", workloadPercentage);
            
        } catch (Exception e) {
            logger.error("Error calculating workload distribution: {}", e.getMessage());
            dashboardStats.put("workloadPercentage", 0.0);
        }
        
        return dashboardStats;
    }

    @Override
    public Issue updateIssueStatus(String issueId, IssueStatus newStatus, String adminUid, String notes) {
        logger.info("Admin [{}] updating issue [{}] status to: {}", adminUid, issueId, newStatus);
        
        try {
            Firestore firestore = FirestoreClient.getFirestore();
            
            // Verify admin exists
            Admin admin = adminRepository.findByUid(adminUid)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with uid: " + adminUid));
            
            // Get the issue
            DocumentReference issueRef = firestore.collection("issues").document(issueId);
            DocumentSnapshot issueDoc = issueRef.get().get();
            
            if (!issueDoc.exists()) {
                throw new ResourceNotFoundException("Issue not found with id: " + issueId);
            }
            
            Issue issue = issueDoc.toObject(Issue.class);
            IssueStatus previousStatus = issue.getStatus();
            
            // Update issue status using the IssueService method with all parameters
            Issue updatedIssue = issueService.updateIssueStatus(issueId, newStatus, adminUid, notes);
            
            // Create status change log
            StatusChangeLog log = new StatusChangeLog(
                issueId,
                previousStatus,
                newStatus,
                adminUid,
                admin.getName(),
                notes
            );
            
            statusChangeLogRepository.save(log);
            
            // Update admin stats
            adminRepository.updateStats(adminUid, previousStatus, newStatus);
            
            // Update the enhanced metrics 
            // These methods would normally be called within the AdminRepository,
            // but for demonstration purposes, we're showing them here
            admin.updatePerformanceMetrics(updatedIssue, newStatus);
            admin.updateCategoryStats(updatedIssue, newStatus);
            admin.updateLocationStats(updatedIssue, newStatus);
            admin.updateTimeMetrics(updatedIssue, newStatus);
            
            // Save the updated admin data
            adminRepository.save(admin);
            
            return updatedIssue;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error updating issue status: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to update issue status: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getIssueStatusHistory(String issueId) {
        logger.info("Getting status history for issue: {}", issueId);
        
        // Get issue first to check if it exists
        try {
            Firestore firestore = FirestoreClient.getFirestore();
            DocumentReference issueRef = firestore.collection("issues").document(issueId);
            DocumentSnapshot issueDoc = issueRef.get().get();
            
            if (!issueDoc.exists()) {
                throw new ResourceNotFoundException("Issue not found with id: " + issueId);
            }
            
            Issue issue = issueDoc.toObject(Issue.class);
            
            // Get status change logs
            List<StatusChangeLog> logs = statusChangeLogRepository.findByIssueId(issueId);
            
            // Sort by date (newest first)
            logs.sort(Comparator.comparing(StatusChangeLog::getChangedAt).reversed());
            
            Map<String, Object> history = new HashMap<>();
            history.put("issue", issue);
            history.put("statusChanges", logs);
            
            // Calculate performance metrics for this specific issue
            if (!logs.isEmpty()) {
                // Time to first response
                StatusChangeLog firstResponse = logs.stream()
                    .filter(log -> log.getPreviousStatus() == IssueStatus.OPEN)
                    .min(Comparator.comparing(StatusChangeLog::getChangedAt))
                    .orElse(null);
                
                if (firstResponse != null && issue.getCreatedAt() != null) {
                    long responseTimeMillis = firstResponse.getChangedAt().getTime() - issue.getCreatedAt().getTime();
                    double responseTimeMinutes = responseTimeMillis / (1000.0 * 60);
                    history.put("responseTimeMinutes", responseTimeMinutes);
                }
                
                // Total resolution time if resolved
                if (issue.getStatus() == IssueStatus.RESOLVED || issue.getStatus() == IssueStatus.CLOSED) {
                    StatusChangeLog resolution = logs.stream()
                        .filter(log -> log.getNewStatus() == IssueStatus.RESOLVED || log.getNewStatus() == IssueStatus.CLOSED)
                        .max(Comparator.comparing(StatusChangeLog::getChangedAt))
                        .orElse(null);
                    
                    if (resolution != null && issue.getCreatedAt() != null) {
                        long resolutionTimeMillis = resolution.getChangedAt().getTime() - issue.getCreatedAt().getTime();
                        double resolutionTimeHours = resolutionTimeMillis / (1000.0 * 60 * 60);
                        history.put("resolutionTimeHours", resolutionTimeHours);
                    }
                }
                
                // Number of status changes
                history.put("totalStatusChanges", logs.size());
            }
            
            return history;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error getting issue status history: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to get issue status history: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getAnalytics(String startDateStr, String endDateStr) {
        logger.info("Getting analytics for period: {} to {}", startDateStr, endDateStr);
        
        try {
            // Parse dates if provided
            Date startDate = null;
            Date endDate = null;
            
            if (startDateStr != null && !startDateStr.isEmpty()) {
                try {
                    startDate = new SimpleDateFormat("yyyy-MM-dd").parse(startDateStr);
                } catch (ParseException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Invalid start date format. Use yyyy-MM-dd");
                }
            }
            
            if (endDateStr != null && !endDateStr.isEmpty()) {
                try {
                    // Add one day to include the end date fully
                    endDate = new SimpleDateFormat("yyyy-MM-dd").parse(endDateStr);
                    Calendar c = Calendar.getInstance();
                    c.setTime(endDate);
                    c.add(Calendar.DATE, 1);
                    endDate = c.getTime();
                } catch (ParseException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Invalid end date format. Use yyyy-MM-dd");
                }
            }
            
            return adminRepository.getAnalytics(startDateStr, endDateStr);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error getting analytics: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to get analytics: " + e.getMessage());
        }
    }
} 