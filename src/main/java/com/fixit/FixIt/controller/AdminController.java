package com.fixit.FixIt.controller;

import com.fixit.FixIt.dto.AdminProfileResponse;
import com.fixit.FixIt.dto.PaginatedResponse;
import com.fixit.FixIt.dto.SuccessResponse;
import com.fixit.FixIt.dto.UpdateIssueStatusRequest;
import com.fixit.FixIt.exception.BadRequestException;
import com.fixit.FixIt.model.Admin;
import com.fixit.FixIt.model.Issue;
import com.fixit.FixIt.service.AdminService;
import com.fixit.FixIt.service.IssueService;
import com.fixit.FixIt.util.ApiResponseUtil;
import com.fixit.FixIt.util.AppConstants;
import com.fixit.FixIt.util.ValidationUtil;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final AdminService adminService;
    private final IssueService issueService;

    public AdminController(AdminService adminService, IssueService issueService) {
        this.adminService = adminService;
        this.issueService = issueService;
    }

    @GetMapping("/profile/{uid}")
    public ResponseEntity<SuccessResponse<AdminProfileResponse>> getAdminProfile(@PathVariable String uid) {
        logger.info("Getting profile for admin: {}", uid);
        
        // Validate UID
        ValidationUtil.validateRequired(uid, "Admin ID");
        
        AdminProfileResponse adminProfile = adminService.getAdminProfile(uid);
        return ApiResponseUtil.success(adminProfile);
    }

    @GetMapping("/dashboard/{uid}")
    public ResponseEntity<SuccessResponse<Map<String, Object>>> getAdminDashboard(@PathVariable String uid) {
        logger.info("Getting dashboard stats for admin: {}", uid);
        
        // Validate UID
        ValidationUtil.validateRequired(uid, "Admin ID");
        
        Map<String, Object> dashboardStats = adminService.getAdminDashboardStats(uid);
        return ApiResponseUtil.success(dashboardStats);
    }

    @GetMapping("/issues")
    public ResponseEntity<SuccessResponse<PaginatedResponse<Issue>>> getIssuesForAdmin(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String reporterUid,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Integer minUpvotes,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        
        logger.info("Admin fetching issues with filters: status={}, location={}, reporterUid={}, searchTerm={}, " +
                "minUpvotes={}, startDate={}, endDate={}, sort={}, page={}, size={}", 
                status, location, reporterUid, searchTerm, minUpvotes, startDate, endDate, sort, page, size);
        
        PaginatedResponse<Issue> paginatedResponse = issueService.getIssues(
                status, location, reporterUid, searchTerm, minUpvotes, 
                startDate, endDate, sort, page, size);
        
        return ApiResponseUtil.success(paginatedResponse);
    }

    @PatchMapping("/issues/{issueId}/status")
    public ResponseEntity<SuccessResponse<Issue>> updateIssueStatus(
            @PathVariable String issueId,
            @Valid @RequestBody UpdateIssueStatusRequest request,
            @RequestHeader("X-Admin-Id") String adminUid) {
        
        logger.info("Admin [{}] updating issue [{}] status to: {}", adminUid, issueId, request.getStatus());
        
        // Validate input
        ValidationUtil.validateRequired(issueId, "Issue ID");
        ValidationUtil.validateRequired(adminUid, "Admin ID");
        ValidationUtil.validateRequired((Object)request.getStatus(), "Status");
        
        
        // Use AdminService's method that handles IssueStatus updates
        Issue updatedIssue = adminService.updateIssueStatus(
            issueId, 
            request.getStatus(),  // This is an IssueStatus enum from the request
            adminUid,
            request.getNotes()
        );
        
        return ApiResponseUtil.success("Issue status updated successfully", updatedIssue);
    }
    
    @GetMapping("/issues/{issueId}/history")
    public ResponseEntity<SuccessResponse<Map<String, Object>>> getIssueStatusHistory(
            @PathVariable String issueId) {
        
        logger.info("Getting status history for issue: {}", issueId);
        
        // Validate input
        ValidationUtil.validateRequired(issueId, "Issue ID");
        
        Map<String, Object> history = adminService.getIssueStatusHistory(issueId);
        return ApiResponseUtil.success(history);
    }
    
    @GetMapping("/analytics")
    public ResponseEntity<SuccessResponse<Map<String, Object>>> getAnalytics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        logger.info("Getting analytics for period: {} to {}", startDate, endDate);
        
        Map<String, Object> analytics = adminService.getAnalytics(startDate, endDate);
        return ApiResponseUtil.success(analytics);
    }
}