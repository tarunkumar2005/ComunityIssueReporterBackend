package com.fixit.FixIt.controller;

import com.fixit.FixIt.dto.CreateIssueRequest;
import com.fixit.FixIt.dto.PaginatedResponse;
import com.fixit.FixIt.dto.UpdateIssueStatusRequest;
import com.fixit.FixIt.dto.UpvoteIssueRequest;
import com.fixit.FixIt.model.Issue;
import com.fixit.FixIt.service.IssueService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/issues")
@CrossOrigin
public class IssueController {

    private final IssueService issueService;
    
    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createIssue(@Valid @RequestBody CreateIssueRequest request) {
        try {
            Issue issue = issueService.createIssue(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", issue);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                .body(Map.of("status", "error", "message", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", "error", "message", "Failed to create issue"));
        }
    }
    
    /**
     * Get issues with optional filtering and sorting
     * 
     * @param status Filter by issue status (OPEN, IN_PROGRESS, RESOLVED, CLOSED, REJECTED)
     * @param location Filter by location
     * @param reporterUid Filter by reporter user ID
     * @param searchTerm Search in title, description, location, and reporter name
     * @param minUpvotes Filter issues with at least this many upvotes
     * @param startDate Filter issues created on or after this date (format: yyyy-MM-dd)
     * @param endDate Filter issues created on or before this date (format: yyyy-MM-dd)
     * @param sort Sort order: "newest", "oldest", "most-upvoted", "least-upvoted", "recently-updated"
     * @param page Page number (1-based)
     * @param size Page size
     * @return Paginated list of issues
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getIssues(
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
        try {
            PaginatedResponse<Issue> paginatedResponse = issueService.getIssues(
                    status, location, reporterUid, searchTerm, minUpvotes, 
                    startDate, endDate, sort, page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", paginatedResponse);
            
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                .body(Map.of("status", "error", "message", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", "error", "message", "Failed to fetch issues"));
        }
    }
    
    @GetMapping("/{issueId}")
    public ResponseEntity<Map<String, Object>> getIssueById(@PathVariable String issueId) {
        try {
            Issue issue = issueService.getIssueById(issueId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", issue);
            
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                .body(Map.of("status", "error", "message", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", "error", "message", "Failed to fetch issue"));
        }
    }
    
    @PatchMapping("/{issueId}/status")
    public ResponseEntity<Map<String, Object>> updateIssueStatus(
            @PathVariable String issueId,
            @Valid @RequestBody UpdateIssueStatusRequest request) {
        try {
            Issue issue = issueService.updateIssueStatus(issueId, request.getStatus());
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", issue);
            
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                .body(Map.of("status", "error", "message", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", "error", "message", "Failed to update issue status"));
        }
    }
    
    @PostMapping("/{issueId}/upvote")
    public ResponseEntity<Map<String, Object>> upvoteIssue(
            @PathVariable String issueId,
            @Valid @RequestBody UpvoteIssueRequest request) {
        try {
            Issue issue = issueService.upvoteIssue(issueId, request.getUserId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", issue);
            
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                .body(Map.of("status", "error", "message", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", "error", "message", "Failed to upvote issue"));
        }
    }
    
    @GetMapping("/{issueId}/upvote/check")
    public ResponseEntity<Map<String, Object>> checkUserUpvote(
            @PathVariable String issueId,
            @RequestParam String userId) {
        try {
            boolean hasUpvoted = issueService.hasUserUpvotedIssue(userId, issueId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("hasUpvoted", hasUpvoted);
            
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                .body(Map.of("status", "error", "message", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", "error", "message", "Failed to check upvote status"));
        }
    }
    
    @DeleteMapping("/{issueId}")
    public ResponseEntity<Map<String, Object>> deleteIssue(
            @PathVariable String issueId,
            @RequestParam String userId) {
        try {
            issueService.deleteIssue(issueId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Issue deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                .body(Map.of("status", "error", "message", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", "error", "message", "Failed to delete issue"));
        }
    }
    
    @PutMapping("/{issueId}")
    public ResponseEntity<Map<String, Object>> updateIssue(
            @PathVariable String issueId,
            @Valid @RequestBody CreateIssueRequest request) {
        try {
            Issue issue = issueService.updateIssue(issueId, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", issue);
            
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                .body(Map.of("status", "error", "message", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", "error", "message", "Failed to update issue"));
        }
    }
}