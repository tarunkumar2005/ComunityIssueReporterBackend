package com.fixit.FixIt.util;

public final class AppConstants {
    
    // General
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_SORT_BY = "createdAt";
    public static final String DEFAULT_SORT_DIRECTION = "desc";
    
    // User messages
    public static final String USER_REGISTERED_SUCCESS = "User registered successfully";
    public static final String USER_LOGGED_IN_SUCCESS = "User logged in successfully";
    public static final String USER_LOGGED_OUT_SUCCESS = "User logged out successfully";
    public static final String PASSWORD_RESET_EMAIL_SENT = "Password reset email has been sent";
    public static final String PASSWORD_UPDATED_SUCCESS = "Password updated successfully";
    public static final String PROFILE_UPDATED_SUCCESS = "Profile updated successfully";
    
    // Admin messages
    public static final String ADMIN_REGISTERED_SUCCESS = "Admin registered successfully";
    public static final String ADMIN_LOGGED_IN_SUCCESS = "Admin logged in successfully";
    
    // Issue messages
    public static final String ISSUE_CREATED_SUCCESS = "Issue created successfully";
    public static final String ISSUE_UPDATED_SUCCESS = "Issue updated successfully";
    public static final String ISSUE_DELETED_SUCCESS = "Issue deleted successfully";
    public static final String ISSUE_STATUS_UPDATED_SUCCESS = "Issue status updated successfully";
    public static final String ISSUE_UPVOTED_SUCCESS = "Issue upvoted successfully";
    
    // Image messages
    public static final String IMAGE_UPLOADED_SUCCESS = "Image uploaded successfully";
    public static final String IMAGE_DELETED_SUCCESS = "Image deleted successfully";
    
    // Collection names
    public static final String USERS_COLLECTION = "users";
    public static final String ISSUES_COLLECTION = "issues";
    public static final String UPVOTES_COLLECTION = "upvotes";
    public static final String ADMINS_COLLECTION = "admins";
    public static final String STATUS_CHANGE_LOGS_COLLECTION = "statusChangeLogs";
    
    // Storage paths
    public static final String ISSUE_IMAGES_PATH = "issue-images";
    
    // Roles
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";
    
    // Stats fields
    public static final String STAT_ISSUES_RESOLVED = "issuesResolved";
    public static final String STAT_ISSUES_CLOSED = "issuesClosed";
    public static final String STAT_ISSUES_REJECTED = "issuesRejected";
    public static final String STAT_TOTAL_ISSUES_HANDLED = "totalIssuesHandled";
    public static final String STAT_APPROVAL_RATE = "approvalRate";
    
    // Other constants
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final String SORT_NEWEST = "newest";
    public static final String SORT_OLDEST = "oldest";
    public static final String SORT_MOST_UPVOTED = "most-upvoted";
    public static final String SORT_LEAST_UPVOTED = "least-upvoted";
    public static final String SORT_RECENTLY_UPDATED = "recently-updated";
    
    // Error Messages
    public static final String USER_NOT_FOUND = "User not found";
    public static final String ISSUE_NOT_FOUND = "Issue not found";
    public static final String INVALID_CREDENTIALS = "Invalid credentials";
    public static final String ACCESS_DENIED = "You don't have permission to access this resource";
    public static final String INVALID_TOKEN = "Invalid or expired token";
    public static final String EMAIL_ALREADY_EXISTS = "Email already in use";
    public static final String USERNAME_ALREADY_EXISTS = "Username already in use";
    public static final String SERVER_ERROR = "Something went wrong. Please try again later";
    public static final String FILE_UPLOAD_ERROR = "Error uploading file";
    public static final String INVALID_FILE_FORMAT = "Invalid file format";
    public static final String OPERATION_NOT_ALLOWED = "Operation not allowed";
    
    private AppConstants() {
        // Private constructor to prevent instantiation
    }
} 