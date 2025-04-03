package com.fixit.FixIt.controller;

import com.fixit.FixIt.dto.AdminAuthResponse;
import com.fixit.FixIt.dto.AdminSignupRequest;
import com.fixit.FixIt.dto.AdminSigninRequest;
import com.fixit.FixIt.dto.SuccessResponse;
import com.fixit.FixIt.exception.BadRequestException;
import com.fixit.FixIt.service.AdminAuthService;
import com.fixit.FixIt.util.ApiResponseUtil;
import com.fixit.FixIt.util.AppConstants;
import com.fixit.FixIt.util.FirebaseErrorMapper;
import com.google.firebase.auth.FirebaseAuthException;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/auth")
@CrossOrigin
public class AdminAuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminAuthController.class);
    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/signup")
    public ResponseEntity<SuccessResponse<AdminAuthResponse>> signup(@Valid @RequestBody AdminSignupRequest request) {
        logger.info("Processing admin signup request for email: {}", request.getEmail());
        
        try {
            AdminAuthResponse authResponse = adminAuthService.createAdmin(
                request.getEmail(), 
                request.getPassword(),
                request.getName(),
                request.getPhoneNumber(),
                request.getLocation()
            );
            
            logger.info("Admin registered successfully with uid: {}", authResponse.getUid());
            
            return ApiResponseUtil.created(AppConstants.ADMIN_REGISTERED_SUCCESS, authResponse);
        } catch (FirebaseAuthException e) {
            logger.error("Firebase auth exception during admin signup: {}", e.getMessage(), e);
            FirebaseErrorMapper.throwMappedException(e.getErrorCode(), "Admin registration failed");
            // The following line will never be executed, as throwMappedException always throws an exception
            return null;
        } catch (Exception e) {
            logger.error("Unexpected exception during admin signup: {}", e.getMessage(), e);
            throw new BadRequestException("Admin registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<SuccessResponse<AdminAuthResponse>> signin(@Valid @RequestBody AdminSigninRequest request) {
        logger.info("Processing admin signin request for email: {}", request.getEmail());
        
        try {
            AdminAuthResponse authResponse = adminAuthService.signInAdmin(
                request.getEmail(), 
                request.getPassword()
            );
            
            logger.info("Admin logged in successfully: {}", authResponse.getUid());
            return ApiResponseUtil.success(AppConstants.ADMIN_LOGGED_IN_SUCCESS, authResponse);
        } catch (FirebaseAuthException e) {
            logger.error("Firebase auth exception during admin signin: {}", e.getMessage(), e);
            FirebaseErrorMapper.throwMappedException(e.getErrorCode(), "Authentication failed");
            // The following line will never be executed, as throwMappedException always throws an exception
            return null;
        } catch (Exception e) {
            logger.error("Unexpected exception during admin signin: {}", e.getMessage(), e);
            throw new BadRequestException("Authentication failed: " + e.getMessage());
        }
    }
    
    @PostMapping("/verify")
    public ResponseEntity<SuccessResponse<Map<String, Boolean>>> verifyAdminToken(@RequestHeader("Authorization") String authHeader) {
        logger.info("Verifying admin token");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BadRequestException("Invalid authorization header");
        }
        
        String token = authHeader.substring(7);
        
        try {
            boolean isValid = adminAuthService.verifyAdminToken(token);
            return ApiResponseUtil.success(Map.of("valid", isValid));
        } catch (Exception e) {
            logger.error("Error verifying admin token: {}", e.getMessage(), e);
            return ApiResponseUtil.success(Map.of("valid", false));
        }
    }
} 