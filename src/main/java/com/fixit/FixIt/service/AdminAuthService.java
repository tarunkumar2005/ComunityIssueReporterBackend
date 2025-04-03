package com.fixit.FixIt.service;

import com.fixit.FixIt.dto.AdminAuthResponse;
import com.google.firebase.auth.FirebaseAuthException;

public interface AdminAuthService {
    
    /**
     * Create a new admin
     * @param email Admin email
     * @param password Admin password
     * @param name Admin name
     * @param phoneNumber Admin phone number
     * @param location Admin location
     * @return AdminAuthResponse containing authentication details
     * @throws FirebaseAuthException If there's an error during admin creation
     */
    AdminAuthResponse createAdmin(String email, String password, String name, String phoneNumber, String location) throws FirebaseAuthException;
    
    /**
     * Sign in an admin
     * @param email Admin email
     * @param password Admin password
     * @return AdminAuthResponse containing authentication details
     * @throws FirebaseAuthException If there's an error during sign in
     */
    AdminAuthResponse signInAdmin(String email, String password) throws FirebaseAuthException;
    
    /**
     * Verify if admin token is valid
     * @param token Admin authentication token
     * @return True if token is valid and belongs to an admin, false otherwise
     * @throws FirebaseAuthException If there's an error during token verification
     */
    boolean verifyAdminToken(String token) throws FirebaseAuthException;
} 