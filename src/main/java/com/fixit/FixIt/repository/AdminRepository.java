package com.fixit.FixIt.repository;

import com.fixit.FixIt.model.Admin;
import com.fixit.FixIt.model.IssueStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AdminRepository {
    
    /**
     * Save an admin to the database
     * @param admin The admin to save
     * @return The saved admin
     */
    Admin save(Admin admin);
    
    /**
     * Find an admin by their UID
     * @param uid The admin's UID
     * @return An Optional containing the admin if found, or empty if not found
     */
    Optional<Admin> findByUid(String uid);
    
    /**
     * Find an admin by their email
     * @param email The admin's email
     * @return An Optional containing the admin if found, or empty if not found
     */
    Optional<Admin> findByEmail(String email);
    
    /**
     * Check if an admin exists by UID
     * @param uid The admin's UID
     * @return True if the admin exists, false otherwise
     */
    boolean existsByUid(String uid);
    
    /**
     * Update an admin's stats based on issue status change
     * @param adminUid The admin's UID
     * @param previousStatus The previous issue status
     * @param newStatus The new issue status
     * @return The updated admin
     */
    Admin updateStats(String adminUid, IssueStatus previousStatus, IssueStatus newStatus);
    
    /**
     * Get analytics data for all admins
     * @param startDate Start date in ISO format (yyyy-MM-dd)
     * @param endDate End date in ISO format (yyyy-MM-dd) 
     * @return Map containing analytics data
     */
    Map<String, Object> getAnalytics(String startDate, String endDate);
    
    /**
     * Get all admins
     * @return List of all admins
     */
    List<Admin> findAll();
    
    /**
     * Delete an admin by their UID
     * @param uid The admin's UID
     * @return True if the admin was deleted, false otherwise
     */
    boolean deleteByUid(String uid);
} 