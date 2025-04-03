package com.fixit.FixIt.controller;

import com.fixit.FixIt.dto.AuthRequest;
import com.fixit.FixIt.dto.AuthResponse;
import com.fixit.FixIt.dto.GoogleAuthRequest;
import com.fixit.FixIt.dto.SuccessResponse;
import com.fixit.FixIt.exception.BadRequestException;
import com.fixit.FixIt.service.FirebaseAuthService;
import com.fixit.FixIt.util.ApiResponseUtil;
import com.fixit.FixIt.util.AppConstants;
import com.fixit.FixIt.util.FirebaseErrorMapper;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final FirebaseAuthService firebaseAuthService;

    public AuthController(FirebaseAuthService firebaseAuthService) {
        this.firebaseAuthService = firebaseAuthService;
    }

    @PostMapping("/signup")
    public ResponseEntity<SuccessResponse<AuthResponse>> signup(@Valid @RequestBody AuthRequest request) {
        logger.info("Processing signup request for email: {}", request.getEmail());
        
        // Validate request
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException("Name is required");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new BadRequestException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new BadRequestException("Password must be at least 6 characters long");
        }

        try {
            // Create user
            UserRecord user = firebaseAuthService.createUser(
                request.getEmail(), 
                request.getPassword(),
                request.getName()
            );
            
            // Sign in the user
            Map<String, Object> userData = firebaseAuthService.signIn(
                request.getEmail(), 
                request.getPassword()
            );
            
            logger.debug("User data from sign in: {}", userData);
            
            // Get required data from response
            String token = (String) userData.get("token");
            String uid = user.getUid();
            String username = (String) userData.get("username");
            
            if (token == null) {
                logger.error("Token is null in signup response");
                throw new BadRequestException("Authentication token missing in response");
            }
            
            if (username == null) {
                logger.warn("Username is null in signup response, attempting to retrieve from Firestore");
                // Try to get username from Firestore directly as fallback
                Map<String, Object> userDataFromFirestore = firebaseAuthService.getUserData(uid);
                username = (String) userDataFromFirestore.get("username");
                
                if (username == null) {
                    // Last resort: generate a username from the name
                    username = request.getName().toLowerCase().replaceAll("[^a-z0-9]", "");
                    logger.info("Generated fallback username: {}", username);
                }
            }
            
            AuthResponse authResponse = new AuthResponse(token, uid, username);
            logger.info("User registered successfully with uid: {}", uid);
            
            return ApiResponseUtil.created(AppConstants.USER_REGISTERED_SUCCESS, authResponse);
        } catch (FirebaseAuthException e) {
            logger.error("Firebase auth exception during signup: {}", e.getMessage(), e);
            FirebaseErrorMapper.throwMappedException(e.getErrorCode(), "Registration failed");
            // The following line will never be executed, as throwMappedException always throws an exception
            return null;
        } catch (Exception e) {
            logger.error("Unexpected exception during signup: {}", e.getMessage(), e);
            throw new BadRequestException("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<SuccessResponse<AuthResponse>> signin(@Valid @RequestBody AuthRequest request) {
        logger.info("Processing signin request");
        
        // Validate request
        if (request.getLoginIdentifier() == null || request.getLoginIdentifier().trim().isEmpty()) {
            throw new BadRequestException("Email or username is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new BadRequestException("Password is required");
        }

        try {
            Map<String, Object> userData = firebaseAuthService.signIn(
                request.getLoginIdentifier(), 
                request.getPassword()
            );
            
            AuthResponse authResponse = new AuthResponse(
                (String) userData.get("token"),
                (String) userData.get("uid"),
                (String) userData.get("username")
            );
            
            logger.info("User logged in successfully: {}", userData.get("uid"));
            return ApiResponseUtil.success(AppConstants.USER_LOGGED_IN_SUCCESS, authResponse);
        } catch (FirebaseAuthException e) {
            logger.error("Firebase auth exception during signin: {}", e.getMessage(), e);
            FirebaseErrorMapper.throwMappedException(e.getErrorCode(), "Authentication failed");
            // The following line will never be executed, as throwMappedException always throws an exception
            return null;
        } catch (Exception e) {
            logger.error("Unexpected exception during signin: {}", e.getMessage(), e);
            throw new BadRequestException("Authentication failed: " + e.getMessage());
        }
    }

    @PostMapping("/google")
    public ResponseEntity<SuccessResponse<AuthResponse>> googleAuth(@Valid @RequestBody GoogleAuthRequest request) {
        logger.info("Processing Google authentication request");
        
        try {
            // Handle Google sign-in with just the token
            Map<String, Object> userData = firebaseAuthService.handleGoogleSignIn(request.getIdToken());
            
            AuthResponse authResponse = new AuthResponse(
                (String) userData.get("token"),
                (String) userData.get("uid"),
                (String) userData.get("username")
            );
            
            logger.info("Google authentication successful for user: {}", userData.get("uid"));
            return ApiResponseUtil.success(AppConstants.USER_LOGGED_IN_SUCCESS, authResponse);
        } catch (Exception e) {
            logger.error("Unexpected exception during Google auth: {}", e.getMessage(), e);
            throw new BadRequestException("Google authentication failed: " + e.getMessage());
        }
    }
}