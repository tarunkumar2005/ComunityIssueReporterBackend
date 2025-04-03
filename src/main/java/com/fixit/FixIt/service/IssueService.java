package com.fixit.FixIt.service;

import com.fixit.FixIt.dto.CreateIssueRequest;
import com.fixit.FixIt.dto.PaginatedResponse;
import com.fixit.FixIt.model.Issue;
import com.fixit.FixIt.model.IssueStatus;
import com.fixit.FixIt.model.UserUpvote;
import com.fixit.FixIt.repository.UserUpvoteRepository;
import com.google.api.core.ApiFuture;
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
public class IssueService {
    
    private static final Logger logger = LoggerFactory.getLogger(IssueService.class);
    private static final String COLLECTION_NAME = "issues";
    private final UserUpvoteRepository userUpvoteRepository;
    
    public IssueService(UserUpvoteRepository userUpvoteRepository) {
        this.userUpvoteRepository = userUpvoteRepository;
    }
    
    public Issue createIssue(CreateIssueRequest request) {
        try {
            logger.info("Creating new issue with title: {}", request.getTitle());
            Firestore firestore = FirestoreClient.getFirestore();
            
            // Create new issue
            Issue issue = new Issue();
            issue.setTitle(request.getTitle());
            issue.setDescription(request.getDescription());
            issue.setLocation(request.getLocation());
            issue.setLatitude(request.getLatitude());
            issue.setLongitude(request.getLongitude());
            issue.setReporterUid(request.getReporterUid());
            issue.setReporterName(request.getReporterName());
            
            if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
                issue.setImageUrls(request.getImageUrls());
            }
            
            // Generate a new document with auto-generated ID
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
            issue.setId(docRef.getId());
            
            // Set the data
            ApiFuture<WriteResult> result = docRef.set(issue);
            
            // Wait for operation to complete
            result.get();
            logger.info("Issue created successfully with ID: {}", issue.getId());
            
            return issue;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error creating issue: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to create issue: " + e.getMessage());
        }
    }
    
    public PaginatedResponse<Issue> getIssues(
            String status, 
            String location, 
            String reporterUid, 
            String searchTerm, 
            Integer minUpvotes, 
            String startDate, 
            String endDate, 
            String sort, 
            int page, 
            int size) {
        try {
            logger.info("Fetching issues with filters - status: {}, location: {}, reporterUid: {}, " +
                    "searchTerm: {}, minUpvotes: {}, startDate: {}, endDate: {}, sort: {}, page: {}, size: {}", 
                    status, location, reporterUid, searchTerm, minUpvotes, startDate, endDate, sort, page, size);
            
            logger.debug("Getting Firestore instance");
            Firestore firestore = FirestoreClient.getFirestore();
            logger.debug("Firestore instance obtained: {}", firestore != null ? "success" : "null");
            
            CollectionReference issuesCollection = firestore.collection(COLLECTION_NAME);
            logger.debug("Collection reference obtained: {}", COLLECTION_NAME);
            
            // Create query
            Query query = issuesCollection;
            
            // Apply status filter if provided
            if (status != null && !status.isEmpty()) {
                try {
                    logger.debug("Applying status filter: {}", status);
                    IssueStatus issueStatus = IssueStatus.valueOf(status.toUpperCase());
                    query = query.whereEqualTo("status", issueStatus);
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid status value: {}", status);
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status value");
                }
            }
            
            // Apply location filter if provided
            if (location != null && !location.isEmpty()) {
                logger.debug("Applying location filter: {}", location);
                query = query.whereEqualTo("location", location);
            }
            
            // Apply reporter filter if provided
            if (reporterUid != null && !reporterUid.isEmpty()) {
                logger.debug("Applying reporter filter: {}", reporterUid);
                query = query.whereEqualTo("reporterUid", reporterUid);
            }
            
            // Apply minimum upvotes filter if provided
            if (minUpvotes != null && minUpvotes > 0) {
                logger.debug("Applying minimum upvotes filter: {}", minUpvotes);
                query = query.whereGreaterThanOrEqualTo("upvotes", minUpvotes);
            }
            
            // Apply date range filters if provided
            if (startDate != null && !startDate.isEmpty()) {
                try {
                    Date start = new SimpleDateFormat("yyyy-MM-dd").parse(startDate);
                    logger.debug("Applying start date filter: {}", start);
                    query = query.whereGreaterThanOrEqualTo("createdAt", start);
                } catch (ParseException e) {
                    logger.warn("Invalid start date format: {}", startDate);
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Invalid start date format. Use yyyy-MM-dd");
                }
            }
            
            if (endDate != null && !endDate.isEmpty()) {
                try {
                    // Add one day to include the end date fully
                    Date end = new SimpleDateFormat("yyyy-MM-dd").parse(endDate);
                    Calendar c = Calendar.getInstance();
                    c.setTime(end);
                    c.add(Calendar.DATE, 1);
                    end = c.getTime();
                    
                    logger.debug("Applying end date filter: {}", end);
                    query = query.whereLessThan("createdAt", end);
                } catch (ParseException e) {
                    logger.warn("Invalid end date format: {}", endDate);
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Invalid end date format. Use yyyy-MM-dd");
                }
            }
            
            // Get all matching documents for filtering by searchTerm and counting
            List<Issue> filteredIssues = new ArrayList<>();
            int totalItems = 0;
            
            // Execute query to get all matching documents (limited to 1000 for performance)
            logger.debug("Executing count query with limit 1000");
            try {
                logger.debug("About to execute get() on query");
                ApiFuture<QuerySnapshot> countFuture = query.limit(1000).get();
                logger.debug("Query future obtained, waiting for result");
                QuerySnapshot countSnapshot = countFuture.get();
                logger.debug("Count query completed, found {} documents", countSnapshot.size());
                
                // Convert to list of issues
                List<Issue> allMatchingIssues = countSnapshot.getDocuments().stream()
                    .map(doc -> doc.toObject(Issue.class))
                    .collect(Collectors.toList());
                
                // Always apply explicit sorting to ensure correct order
                logger.info("Applying explicit sorting to results: {}", sort);
                if ("newest".equals(sort)) {
                    allMatchingIssues.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
                } else if ("oldest".equals(sort)) {
                    allMatchingIssues.sort((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
                } else if ("most-upvoted".equals(sort)) {
                    allMatchingIssues.sort((a, b) -> b.getUpvotes().compareTo(a.getUpvotes()));
                } else if ("least-upvoted".equals(sort)) {
                    allMatchingIssues.sort((a, b) -> a.getUpvotes().compareTo(b.getUpvotes()));
                } else if ("recently-updated".equals(sort)) {
                    allMatchingIssues.sort((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()));
                } else {
                    // Default sorting by newest
                    allMatchingIssues.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
                }
                
                // Apply text search filter if provided (client-side filtering)
                if (searchTerm != null && !searchTerm.isEmpty()) {
                    logger.debug("Applying search term filter: {}", searchTerm);
                    String searchLower = searchTerm.toLowerCase();
                    filteredIssues = allMatchingIssues.stream()
                        .filter(issue -> 
                            (issue.getTitle() != null && issue.getTitle().toLowerCase().contains(searchLower)) ||
                            (issue.getDescription() != null && issue.getDescription().toLowerCase().contains(searchLower)) ||
                            (issue.getLocation() != null && issue.getLocation().toLowerCase().contains(searchLower)) ||
                            (issue.getReporterName() != null && issue.getReporterName().toLowerCase().contains(searchLower))
                        )
                        .collect(Collectors.toList());
                    
                    // No need to re-sort here as we've already sorted allMatchingIssues
                    totalItems = filteredIssues.size();
                } else {
                    filteredIssues = allMatchingIssues;
                    totalItems = allMatchingIssues.size();
                }
                
                // Apply pagination to the filtered results
                int fromIndex = (page - 1) * size;
                int toIndex = Math.min(fromIndex + size, filteredIssues.size());
                
                // Handle invalid page number
                if (fromIndex >= filteredIssues.size()) {
                    if (!filteredIssues.isEmpty()) {
                        // If requesting a page beyond available data but there is data,
                        // return the last page
                        fromIndex = ((totalItems - 1) / size) * size;
                        toIndex = totalItems;
                    } else {
                        // If no data, return empty list
                        fromIndex = 0;
                        toIndex = 0;
                    }
                }
                
                List<Issue> paginatedIssues = (fromIndex < toIndex) 
                    ? filteredIssues.subList(fromIndex, toIndex) 
                    : new ArrayList<>();
                
                logger.info("Successfully fetched {} issues (page {}/{}, total items: {})", 
                        paginatedIssues.size(), page, (int) Math.ceil((double) totalItems / size), totalItems);
                
                return new PaginatedResponse<>(paginatedIssues, totalItems, page, size);
            } catch (ExecutionException e) {
                logger.error("ExecutionException while fetching issues: {}", e.getMessage(), e);
                if (e.getCause() != null) {
                    logger.error("Cause: {}", e.getCause().getMessage(), e.getCause());
                    if (e.getCause().getCause() != null) {
                        logger.error("Root cause: {}", e.getCause().getCause().getMessage(), e.getCause().getCause());
                    }
                }
                throw e;
            }
        } catch (ResponseStatusException e) {
            logger.error("ResponseStatusException while fetching issues: {}", e.getReason(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Exception while fetching issues: {}", e.getMessage(), e);
            if (e.getCause() != null) {
                logger.error("Cause: {}", e.getCause().getMessage(), e.getCause());
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to fetch issues: " + e.getMessage());
        }
    }
    
    public Issue getIssueById(String issueId) {
        try {
            Firestore firestore = FirestoreClient.getFirestore();
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(issueId);
            DocumentSnapshot document = docRef.get().get();
            
            if (!document.exists()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Issue not found");
            }
            
            return document.toObject(Issue.class);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to fetch issue: " + e.getMessage());
        }
    }
    
    /**
     * Updates the status of an issue
     * @param issueId ID of the issue to update
     * @param status New status as a string (must be a valid IssueStatus)
     * @return Updated Issue
     */
    public Issue updateIssueStatus(String issueId, String status) {
        try {
            IssueStatus issueStatus;
            try {
                issueStatus = IssueStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status value");
            }
            return updateIssueStatus(issueId, issueStatus);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to update issue status: " + e.getMessage());
        }
    }
    
    /**
     * Updates the status of an issue (overloaded method accepting IssueStatus directly)
     * @param issueId ID of the issue to update
     * @param status New status as IssueStatus enum
     * @return Updated Issue
     */
    public Issue updateIssueStatus(String issueId, IssueStatus status) {
        try {
            Firestore firestore = FirestoreClient.getFirestore();
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(issueId);
            DocumentSnapshot document = docRef.get().get();
            
            if (!document.exists()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Issue not found");
            }
            
            // Update issue status
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", status);
            updates.put("updatedAt", new Date());
            
            docRef.update(updates).get();
            
            // Fetch updated issue
            document = docRef.get().get();
            return document.toObject(Issue.class);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to update issue status: " + e.getMessage());
        }
    }
    
    /**
     * Updates the status of an issue with admin information
     * @param issueId ID of the issue to update
     * @param status New status as IssueStatus enum
     * @param adminUid Admin UID who is making the change
     * @param notes Optional notes for the status change
     * @return Updated Issue
     */
    public Issue updateIssueStatus(String issueId, IssueStatus status, String adminUid, String notes) {
        try {
            Firestore firestore = FirestoreClient.getFirestore();
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(issueId);
            DocumentSnapshot document = docRef.get().get();
            
            if (!document.exists()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Issue not found");
            }
            
            // Update all fields at once for better efficiency
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", status);
            updates.put("handledByAdminUid", adminUid);
            updates.put("adminNotes", notes);
            updates.put("lastStatusChangeAt", new Date());
            updates.put("updatedAt", new Date());
            
            docRef.update(updates).get();
            
            // Fetch updated issue
            document = docRef.get().get();
            return document.toObject(Issue.class);
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error updating issue admin info: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to update issue admin info: " + e.getMessage());
        }
    }
    
    public Issue upvoteIssue(String issueId, String userId) {
        try {
            // Check if user has already upvoted this issue
            if (userUpvoteRepository.hasUserUpvotedIssue(userId, issueId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "You have already upvoted this issue");
            }
            
            Firestore firestore = FirestoreClient.getFirestore();
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(issueId);
            DocumentSnapshot document = docRef.get().get();
            
            if (!document.exists()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Issue not found");
            }
            
            Issue issue = document.toObject(Issue.class);
            Integer currentUpvotes = issue.getUpvotes();
            
            // Record the user's upvote
            UserUpvote userUpvote = new UserUpvote(userId, issueId);
            userUpvoteRepository.save(userUpvote);
            
            // Increment upvotes
            Map<String, Object> updates = new HashMap<>();
            updates.put("upvotes", currentUpvotes + 1);
            updates.put("updatedAt", new Date());
            
            docRef.update(updates).get();
            
            // Fetch updated issue
            document = docRef.get().get();
            return document.toObject(Issue.class);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to upvote issue: " + e.getMessage());
        }
    }
    
    public Issue addImageToIssue(String issueId, String imageUrl) {
        try {
            Firestore firestore = FirestoreClient.getFirestore();
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(issueId);
            DocumentSnapshot document = docRef.get().get();
            
            if (!document.exists()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Issue not found");
            }
            
            Issue issue = document.toObject(Issue.class);
            List<String> imageUrls = issue.getImageUrls();
            if (imageUrls == null) {
                imageUrls = new ArrayList<>();
            }
            imageUrls.add(imageUrl);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("imageUrls", imageUrls);
            updates.put("updatedAt", new Date());
            
            docRef.update(updates).get();
            
            // Fetch updated issue
            document = docRef.get().get();
            return document.toObject(Issue.class);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to add image to issue: " + e.getMessage());
        }
    }
    
    public boolean hasUserUpvotedIssue(String userId, String issueId) {
        return userUpvoteRepository.hasUserUpvotedIssue(userId, issueId);
    }
    
    public void deleteIssue(String issueId, String userId) {
        try {
            Firestore firestore = FirestoreClient.getFirestore();
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(issueId);
            DocumentSnapshot document = docRef.get().get();
            
            if (!document.exists()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Issue not found");
            }
            
            Issue issue = document.toObject(Issue.class);
            
            // Check if the user is the owner of the issue
            if (!issue.getReporterUid().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "You don't have permission to delete this issue");
            }
            
            // Delete the issue
            docRef.delete().get();
            
            // Delete all upvotes for this issue
            // This would be a good place to use a transaction or batch write
            // to ensure atomicity, but for simplicity we'll just delete them separately
            userUpvoteRepository.deleteByIssueId(issueId);
            
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to delete issue: " + e.getMessage());
        }
    }
    
    public Issue updateIssue(String issueId, CreateIssueRequest request) {
        try {
            Firestore firestore = FirestoreClient.getFirestore();
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(issueId);
            DocumentSnapshot document = docRef.get().get();
            
            if (!document.exists()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Issue not found");
            }
            
            Issue existingIssue = document.toObject(Issue.class);
            
            // Check if the user is the owner of the issue
            if (!existingIssue.getReporterUid().equals(request.getReporterUid())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "You don't have permission to update this issue");
            }
            
            // Update issue fields
            Map<String, Object> updates = new HashMap<>();
            updates.put("title", request.getTitle());
            updates.put("description", request.getDescription());
            updates.put("location", request.getLocation());
            updates.put("latitude", request.getLatitude());
            updates.put("longitude", request.getLongitude());
            updates.put("updatedAt", new Date());
            
            // Only update image URLs if provided
            if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
                updates.put("imageUrls", request.getImageUrls());
            }
            
            docRef.update(updates).get();
            
            // Fetch updated issue
            document = docRef.get().get();
            return document.toObject(Issue.class);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to update issue: " + e.getMessage());
        }
    }
}