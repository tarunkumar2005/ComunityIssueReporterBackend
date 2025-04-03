package com.fixit.FixIt.repository;

import com.fixit.FixIt.model.StatusChangeLog;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface StatusChangeLogRepository {
    
    /**
     * Save a status change log to the database
     * @param log The log to save
     * @return The saved log
     */
    StatusChangeLog save(StatusChangeLog log);
    
    /**
     * Find a status change log by its ID
     * @param id The log ID
     * @return An Optional containing the log if found, or empty if not found
     */
    Optional<StatusChangeLog> findById(String id);
    
    /**
     * Find all status change logs for an issue
     * @param issueId The issue ID
     * @return List of status change logs for the issue
     */
    List<StatusChangeLog> findByIssueId(String issueId);
    
    /**
     * Find all status change logs by admin UID
     * @param adminUid The admin UID
     * @return List of status change logs for the admin
     */
    List<StatusChangeLog> findByChangedByAdminUid(String adminUid);
    
    /**
     * Find all status change logs by admin UID after a specific date
     * @param adminUid The admin UID
     * @param date The date after which to find logs
     * @return List of status change logs for the admin after the specified date
     */
    List<StatusChangeLog> findByChangedByAdminUidAndChangedAtAfter(String adminUid, Date date);
    
    /**
     * Delete all logs for a specific issue
     * @param issueId The issue ID
     * @return Number of logs deleted
     */
    int deleteByIssueId(String issueId);
} 