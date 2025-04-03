package com.fixit.FixIt.service;

import com.fixit.FixIt.dto.AdminAuthResponse;
import com.fixit.FixIt.model.Admin;
import com.fixit.FixIt.repository.AdminRepository;
import com.fixit.FixIt.exception.BadRequestException;
import com.fixit.FixIt.exception.ResourceNotFoundException;
import com.fixit.FixIt.exception.UnauthorizedException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;
import com.google.firebase.auth.UserRecord.UpdateRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AdminAuthServiceImpl implements AdminAuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminAuthServiceImpl.class);
    private final AdminRepository adminRepository;
    
    public AdminAuthServiceImpl(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Override
    public AdminAuthResponse createAdmin(String email, String password, String name, String phoneNumber, String location) throws FirebaseAuthException {
        logger.info("Creating admin with email: {}", email);
        
        // Check if admin already exists with this email
        if (adminRepository.findByEmail(email).isPresent()) {
            throw new BadRequestException("Admin with this email already exists");
        }
        
        try {
            // Format phone number to ensure E.164 compliance
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                // If phone number doesn't start with +, check if it's a 10-digit number and add +91
                if (!phoneNumber.startsWith("+")) {
                    if (phoneNumber.matches("\\d{10}")) {
                        phoneNumber = "+91" + phoneNumber;
                        logger.info("Formatted phone number with +91 prefix: {}", phoneNumber);
                    } else {
                        throw new BadRequestException("Phone number must be a 10-digit number if not in E.164 format");
                    }
                }
            }
            
            // Create user in Firebase Authentication
            CreateRequest request = new CreateRequest()
                .setEmail(email)
                .setPassword(password)
                .setDisplayName(name)
                .setPhoneNumber(phoneNumber)
                .setEmailVerified(true);
            
            UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
            String uid = userRecord.getUid();
            
            // Set custom claims to identify as admin
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", "ADMIN");
            FirebaseAuth.getInstance().setCustomUserClaims(uid, claims);
            
            // Create custom token
            String token = FirebaseAuth.getInstance().createCustomToken(uid, claims);
            
            // Create admin in Firestore
            Admin admin = new Admin();
            admin.setUid(uid);
            admin.setEmail(email);
            admin.setName(name);
            admin.setPhoneNumber(phoneNumber);
            admin.setLocation(location);
            admin.setCreatedAt(new Date());
            admin.setLastLogin(new Date());
            
            // Save to Firestore
            adminRepository.save(admin);
            
            // Create response
            AdminAuthResponse response = new AdminAuthResponse();
            response.setToken(token);
            response.setUid(uid);
            response.setName(name);
            response.setPhoneNumber(phoneNumber);
            response.setLocation(location);
            response.setRole("ADMIN");
            response.setPermissions(admin.getPermissions());
            response.setStats(admin.getStats());
            
            return response;
        } catch (FirebaseAuthException e) {
            logger.error("Firebase auth error creating admin: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Error creating admin: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to create admin: " + e.getMessage());
        }
    }

    @Override
    public AdminAuthResponse signInAdmin(String email, String password) throws FirebaseAuthException {
        logger.info("Signing in admin with email: {}", email);
        
        try {
            // Get user by email
            UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(email);
            String uid = userRecord.getUid();
            
            // Verify user has admin role
            Admin admin = adminRepository.findByUid(uid)
                .orElseThrow(() -> new UnauthorizedException("User is not registered as an admin"));
            
            // Create custom token
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", "ADMIN");
            String token = FirebaseAuth.getInstance().createCustomToken(uid, claims);
            
            // Update last login
            admin.setLastLogin(new Date());
            adminRepository.save(admin);
            
            // Create response
            AdminAuthResponse response = new AdminAuthResponse();
            response.setToken(token);
            response.setUid(uid);
            response.setName(admin.getName());
            response.setPhoneNumber(admin.getPhoneNumber());
            response.setLocation(admin.getLocation());
            response.setRole("ADMIN");
            response.setPermissions(admin.getPermissions());
            response.setStats(admin.getStats());
            
            return response;
        } catch (FirebaseAuthException e) {
            logger.error("Firebase auth error signing in admin: {}", e.getMessage(), e);
            throw e;
        } catch (UnauthorizedException e) {
            logger.error("Unauthorized admin sign in attempt: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Error signing in admin: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to sign in: " + e.getMessage());
        }
    }

    @Override
    public boolean verifyAdminToken(String token) throws FirebaseAuthException {
        try {
            // Verify ID token
            var firebaseToken = FirebaseAuth.getInstance().verifyIdToken(token);
            String uid = firebaseToken.getUid();
            
            // Check if user has admin role
            Map<String, Object> claims = firebaseToken.getClaims();
            boolean isAdmin = claims.containsKey("role") && "ADMIN".equals(claims.get("role"));
            
            if (!isAdmin) {
                // Check in database to be sure
                return adminRepository.existsByUid(uid);
            }
            
            return true;
        } catch (FirebaseAuthException e) {
            logger.error("Firebase auth error verifying admin token: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Error verifying admin token: {}", e.getMessage(), e);
            return false;
        }
    }
} 