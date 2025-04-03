package com.fixit.FixIt.controller;

import com.fixit.FixIt.dto.SuccessResponse;
import com.fixit.FixIt.dto.UserProfileRequest;
import com.fixit.FixIt.dto.UserProfileResponse;
import com.fixit.FixIt.exception.BadRequestException;
import com.fixit.FixIt.exception.ResourceNotFoundException;
import com.fixit.FixIt.model.User;
import com.fixit.FixIt.service.UserService;
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
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile/{uid}")
    public ResponseEntity<SuccessResponse<UserProfileResponse>> getUserProfile(@PathVariable String uid) {
        logger.info("Getting profile for user: {}", uid);
        
        // Validate UID
        ValidationUtil.validateRequired(uid, "User ID");
        
        UserProfileResponse userProfile = userService.getUserProfileWithStats(uid);
        return ApiResponseUtil.success(userProfile);
    }

    @PutMapping("/profile/{uid}")
    public ResponseEntity<SuccessResponse<UserProfileResponse>> updateUserProfile(
            @PathVariable String uid,
            @Valid @RequestBody UserProfileRequest request) {
        logger.info("Updating profile for user: {}", uid);
        
        // Validate UID
        ValidationUtil.validateRequired(uid, "User ID");
        
        // Additional validation for request content
        if (request == null) {
            throw new BadRequestException("Profile update request cannot be empty");
        }
        
        // Validate that at least one field is provided for update
        if ((request.getName() == null || request.getName().trim().isEmpty()) && 
            (request.getNotificationPreferences() == null || request.getNotificationPreferences().isEmpty())) {
            throw new BadRequestException("At least one field must be provided for update");
        }
        
        // First update the user profile
        userService.updateUserProfile(uid, request);
        
        // Then get the updated profile with stats to return
        UserProfileResponse updatedProfile = userService.getUserProfileWithStats(uid);
        
        return ApiResponseUtil.success(AppConstants.PROFILE_UPDATED_SUCCESS, updatedProfile);
    }

    @PutMapping("/profile/{uid}/notifications")
    public ResponseEntity<SuccessResponse<UserProfileResponse>> updateNotificationPreferences(
            @PathVariable String uid,
            @RequestBody Map<String, Boolean> notificationPreferences) {
        logger.info("Updating notification preferences for user: {}", uid);
        
        // Validate UID
        ValidationUtil.validateRequired(uid, "User ID");
        
        // Validate notification preferences
        if (notificationPreferences == null || notificationPreferences.isEmpty()) {
            throw new BadRequestException("Notification preferences cannot be empty");
        }
        
        // Update notification preferences
        userService.updateNotificationPreferences(uid, notificationPreferences);
        
        // Get the updated profile with stats to return
        UserProfileResponse updatedProfile = userService.getUserProfileWithStats(uid);
        
        return ApiResponseUtil.success("Notification preferences updated successfully", updatedProfile);
    }

    @DeleteMapping("/profile/{uid}")
    public ResponseEntity<SuccessResponse<Void>> deleteUserAccount(@PathVariable String uid) {
        logger.info("Deleting account for user: {}", uid);
        
        // Validate UID
        ValidationUtil.validateRequired(uid, "User ID");
        
        userService.deleteUserAccount(uid);
        return ApiResponseUtil.success("Account deleted successfully");
    }
}
