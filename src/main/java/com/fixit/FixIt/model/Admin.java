package com.fixit.FixIt.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Admin {
    private String uid;
    private String email;
    private String name;
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
    
    public Admin() {
        this.createdAt = new Date();
        this.lastLogin = new Date();
        this.role = "ADMIN";
        this.permissions = new HashMap<>();
        this.stats = new HashMap<>();
        this.performanceMetrics = new HashMap<>();
        this.timeMetrics = new HashMap<>();
        this.categoryStats = new HashMap<>();
        this.locationStats = new HashMap<>();
        
        // Default admin permissions
        this.permissions.put("canManageIssues", true);
        this.permissions.put("canManageUsers", true);
        
        // Default stats
        this.stats.put("issuesResolved", 0);
        this.stats.put("issuesClosed", 0);
        this.stats.put("issuesRejected", 0);
        this.stats.put("totalIssuesHandled", 0);
        this.stats.put("approvalRate", 0.0);
        this.stats.put("issuesInProgress", 0);
        this.stats.put("highPriorityIssuesHandled", 0);
        
        // Performance metrics
        this.performanceMetrics.put("averageResolutionTimeInHours", 0.0);
        this.performanceMetrics.put("issuesSolvedFirstAttempt", 0);
        this.performanceMetrics.put("reopenRate", 0.0);
        this.performanceMetrics.put("userSatisfactionScore", 0.0);
        this.performanceMetrics.put("avgResponseTimeInMinutes", 0.0);
        this.performanceMetrics.put("slaComplianceRate", 0.0);
        this.performanceMetrics.put("escalationRate", 0.0);
        
        // Time metrics
        this.timeMetrics.put("issuesPerDay", new HashMap<String, Integer>());
        this.timeMetrics.put("peakHours", new HashMap<String, Integer>());
        this.timeMetrics.put("weekdayVsWeekend", new HashMap<String, Integer>());
        this.timeMetrics.put("monthlyActivity", new HashMap<String, Integer>());
        
        // Category stats
        this.categoryStats.put("byIssueType", new HashMap<String, Integer>());
        this.categoryStats.put("byPriority", new HashMap<String, Integer>());
        this.categoryStats.put("bySeverity", new HashMap<String, Integer>());
        
        // Location stats
        this.locationStats.put("issuesByDistrict", new HashMap<String, Integer>());
        this.locationStats.put("issuesByCity", new HashMap<String, Integer>());
        this.locationStats.put("hotspotAreas", new HashMap<String, Integer>());
    }
    
    // Getters and Setters
    public String getUid() {
        return uid;
    }
    
    public void setUid(String uid) {
        this.uid = uid;
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
    
    public void updateStats(IssueStatus previousStatus, IssueStatus newStatus) {
        int issuesResolved = (int) this.stats.getOrDefault("issuesResolved", 0);
        int issuesClosed = (int) this.stats.getOrDefault("issuesClosed", 0);
        int issuesRejected = (int) this.stats.getOrDefault("issuesRejected", 0);
        int totalIssuesHandled = (int) this.stats.getOrDefault("totalIssuesHandled", 0);
        int issuesInProgress = (int) this.stats.getOrDefault("issuesInProgress", 0);
        
        // If this is a new status change (not just updating the same status)
        if (previousStatus != newStatus) {
            // If changing to a resolved/closed/rejected status
            if (newStatus == IssueStatus.RESOLVED) {
                issuesResolved++;
                totalIssuesHandled++;
                if (previousStatus == IssueStatus.IN_PROGRESS) {
                    issuesInProgress--;
                }
            } else if (newStatus == IssueStatus.CLOSED) {
                issuesClosed++;
                totalIssuesHandled++;
                if (previousStatus == IssueStatus.IN_PROGRESS) {
                    issuesInProgress--;
                }
            } else if (newStatus == IssueStatus.REJECTED) {
                issuesRejected++;
                totalIssuesHandled++;
                if (previousStatus == IssueStatus.IN_PROGRESS) {
                    issuesInProgress--;
                }
            } else if (newStatus == IssueStatus.IN_PROGRESS) {
                issuesInProgress++;
            }
            
            // Update the stats map
            this.stats.put("issuesResolved", issuesResolved);
            this.stats.put("issuesClosed", issuesClosed);
            this.stats.put("issuesRejected", issuesRejected);
            this.stats.put("totalIssuesHandled", totalIssuesHandled);
            this.stats.put("issuesInProgress", issuesInProgress);
            
            // Calculate approval rate
            double approvalRate = totalIssuesHandled > 0 
                ? (double) issuesResolved / totalIssuesHandled * 100.0 
                : 0.0;
            this.stats.put("approvalRate", approvalRate);
        }
    }
    
    public void updatePerformanceMetrics(Issue issue, IssueStatus newStatus) {
        // Update resolution time if issue is being resolved
        if (newStatus == IssueStatus.RESOLVED && issue.getCreatedAt() != null) {
            double currentAvgTime = (double) this.performanceMetrics.getOrDefault("averageResolutionTimeInHours", 0.0);
            int totalResolved = (int) this.stats.getOrDefault("issuesResolved", 0);
            
            if (totalResolved > 0) {
                // Calculate time to resolve this issue in hours
                long createdTime = issue.getCreatedAt().getTime();
                long resolvedTime = new Date().getTime();
                double hoursToResolve = (resolvedTime - createdTime) / (1000.0 * 60 * 60);
                
                // Update average resolution time
                double newAvgTime;
                if (totalResolved == 1) {
                    newAvgTime = hoursToResolve;
                } else {
                    // Weighted average
                    newAvgTime = (currentAvgTime * (totalResolved - 1) + hoursToResolve) / totalResolved;
                }
                
                this.performanceMetrics.put("averageResolutionTimeInHours", newAvgTime);
            }
        }
        
        // Update SLA compliance rate (example calculation)
        // This would need to be adjusted based on actual SLA definitions
        int totalIssues = (int) this.stats.getOrDefault("totalIssuesHandled", 0);
        int compliantIssues = (int) this.performanceMetrics.getOrDefault("slaCompliantIssues", 0);
        
        if (newStatus == IssueStatus.RESOLVED || newStatus == IssueStatus.CLOSED) {
            // Check if resolved within SLA (example: 24 hours)
            if (issue.getCreatedAt() != null) {
                long createdTime = issue.getCreatedAt().getTime();
                long resolvedTime = new Date().getTime();
                double hoursToResolve = (resolvedTime - createdTime) / (1000.0 * 60 * 60);
                
                if (hoursToResolve <= 24.0) { // SLA threshold example
                    compliantIssues++;
                    this.performanceMetrics.put("slaCompliantIssues", compliantIssues);
                }
            }
            
            if (totalIssues > 0) {
                double slaRate = (double) compliantIssues / totalIssues * 100.0;
                this.performanceMetrics.put("slaComplianceRate", slaRate);
            }
        }
    }
    
    public void updateCategoryStats(Issue issue, IssueStatus newStatus) {
        if (newStatus == IssueStatus.RESOLVED || newStatus == IssueStatus.CLOSED) {
            // Update by issue type
            if (issue.getType() != null) {
                Map<String, Integer> byType = (Map<String, Integer>) this.categoryStats.getOrDefault("byIssueType", new HashMap<String, Integer>());
                String type = issue.getType();
                byType.put(type, byType.getOrDefault(type, 0) + 1);
                this.categoryStats.put("byIssueType", byType);
            }
            
            // Update by priority
            if (issue.getPriority() != null) {
                Map<String, Integer> byPriority = (Map<String, Integer>) this.categoryStats.getOrDefault("byPriority", new HashMap<String, Integer>());
                String priority = issue.getPriority();
                byPriority.put(priority, byPriority.getOrDefault(priority, 0) + 1);
                this.categoryStats.put("byPriority", byPriority);
                
                // Update high priority count
                if ("HIGH".equals(issue.getPriority())) {
                    int highPriorityCount = (int) this.stats.getOrDefault("highPriorityIssuesHandled", 0);
                    this.stats.put("highPriorityIssuesHandled", highPriorityCount + 1);
                }
            }
        }
    }
    
    public void updateLocationStats(Issue issue, IssueStatus newStatus) {
        if (newStatus == IssueStatus.RESOLVED || newStatus == IssueStatus.CLOSED) {
            // Update by location
            if (issue.getLocation() != null) {
                String location = issue.getLocation();
                
                // Update by district (assuming location format contains district info)
                String district = extractDistrict(location); // This would need a helper method
                if (district != null) {
                    Map<String, Integer> byDistrict = (Map<String, Integer>) this.locationStats.getOrDefault("issuesByDistrict", new HashMap<String, Integer>());
                    byDistrict.put(district, byDistrict.getOrDefault(district, 0) + 1);
                    this.locationStats.put("issuesByDistrict", byDistrict);
                }
                
                // Update by city
                String city = extractCity(location); // This would need a helper method
                if (city != null) {
                    Map<String, Integer> byCity = (Map<String, Integer>) this.locationStats.getOrDefault("issuesByCity", new HashMap<String, Integer>());
                    byCity.put(city, byCity.getOrDefault(city, 0) + 1);
                    this.locationStats.put("issuesByCity", byCity);
                }
            }
        }
    }
    
    public void updateTimeMetrics(Issue issue, IssueStatus newStatus) {
        if (newStatus == IssueStatus.RESOLVED || newStatus == IssueStatus.CLOSED) {
            Date currentDate = new Date();
            
            // Update issues per day
            String dayKey = String.format("%tF", currentDate); // Format as YYYY-MM-DD
            Map<String, Integer> perDay = (Map<String, Integer>) this.timeMetrics.getOrDefault("issuesPerDay", new HashMap<String, Integer>());
            perDay.put(dayKey, perDay.getOrDefault(dayKey, 0) + 1);
            this.timeMetrics.put("issuesPerDay", perDay);
            
            // Update monthly activity
            String monthKey = String.format("%tY-%tm", currentDate, currentDate); // Format as YYYY-MM
            Map<String, Integer> monthly = (Map<String, Integer>) this.timeMetrics.getOrDefault("monthlyActivity", new HashMap<String, Integer>());
            monthly.put(monthKey, monthly.getOrDefault(monthKey, 0) + 1);
            this.timeMetrics.put("monthlyActivity", monthly);
        }
    }
    
    // Helper methods for location extraction
    private String extractDistrict(String location) {
        // This is a simplified example
        // In a real application, you might use a more sophisticated approach or API
        if (location != null && location.contains(",")) {
            String[] parts = location.split(",");
            if (parts.length >= 2) {
                return parts[1].trim();
            }
        }
        return null;
    }
    
    private String extractCity(String location) {
        // This is a simplified example
        if (location != null && location.contains(",")) {
            String[] parts = location.split(",");
            if (parts.length >= 1) {
                return parts[0].trim();
            }
        }
        return null;
    }
} 