package com.fixit.FixIt.service;

import com.fixit.FixIt.dto.UserProfileRequest;
import com.fixit.FixIt.dto.UserProfileResponse;
import com.fixit.FixIt.exception.ResourceNotFoundException;
import com.fixit.FixIt.model.Issue;
import com.fixit.FixIt.model.IssueStatus;
import com.fixit.FixIt.model.User;
import com.fixit.FixIt.repository.UserRepository;
import com.fixit.FixIt.util.AppConstants;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.cloud.FirestoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.HashMap;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final FirebaseAuthService firebaseAuthService;

    public UserService(UserRepository userRepository, FirebaseAuthService firebaseAuthService) {
        this.userRepository = userRepository;
        this.firebaseAuthService = firebaseAuthService;
    }

    public User registerUser(String username, String password, String email) {
        try {
            // Create user in Firebase Authentication
            var firebaseUser = firebaseAuthService.createUser(email, password, username);

            // Create user in Firestore
            User user = new User(firebaseUser.getUid(), username, email);
            return userRepository.save(user);
        } catch (FirebaseAuthException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error registering user: " + e.getMessage());
        }
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(String uid) {
        return userRepository.findById(uid);
    }

    public User getUserProfile(String uid) {
        try {
            // Get user from Firestore
            User user = userRepository.findById(uid)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.USER_NOT_FOUND));
            
            // Update last login time
            user.setLastLogin(new Date());
            userRepository.save(user);
            
            return user;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving user profile: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving user profile: " + e.getMessage());
        }
    }
    
    public UserProfileResponse getUserProfileWithStats(String uid) {
        try {
            logger.info("Getting user profile with stats for user: {}", uid);
            // Get user from Firestore
            User user = userRepository.findById(uid)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.USER_NOT_FOUND));
            
            // Update last login time
            user.setLastLogin(new Date());
            userRepository.save(user);
            
            // Create response DTO
            UserProfileResponse response = new UserProfileResponse();
            response.setUid(user.getUid());
            response.setName(user.getName());
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            response.setJoinedDate(user.getAccountCreationDate());
            response.setNotificationPreferences(user.getNotificationPreferences());
            
            // Get issue statistics
            int[] issueStats = getIssueStatistics(uid);
            response.setIssuesReported(issueStats[0]);
            response.setIssuesResolved(issueStats[1]);
            
            return response;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving user profile with stats: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error retrieving user profile: " + e.getMessage());
        }
    }
    
    private int[] getIssueStatistics(String uid) throws ExecutionException, InterruptedException {
        logger.info("Getting issue statistics for user: {}", uid);
        int[] stats = new int[2]; // [total, resolved]
        
        // Query Firestore for issues created by this user
        CollectionReference issuesCollection = FirestoreClient.getFirestore().collection("issues");
        
        // Get total reported issues
        QuerySnapshot totalSnapshot = issuesCollection
            .whereEqualTo("reporterUid", uid)
            .get()
            .get();
        stats[0] = totalSnapshot.size();
        
        // Get resolved issues
        QuerySnapshot resolvedSnapshot = issuesCollection
            .whereEqualTo("reporterUid", uid)
            .whereEqualTo("status", IssueStatus.RESOLVED)
            .get()
            .get();
        stats[1] = resolvedSnapshot.size();
        
        logger.info("User {} has reported {} issues, with {} resolved", uid, stats[0], stats[1]);
        return stats;
    }

    public User updateUserProfile(String uid, UserProfileRequest request) {
        try {
            logger.info("Updating profile for user: {}", uid);
            // Validate if user exists
            User user = userRepository.findById(uid)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.USER_NOT_FOUND));
            
            // Update name if provided and different from current name
            if (request.getName() != null && !request.getName().trim().isEmpty()) {
                String newName = request.getName().trim();
                
                // Only update if the name is actually different
                if (!newName.equals(user.getName())) {
                    logger.debug("Updating name from '{}' to '{}'", user.getName(), newName);
                    user.setName(newName);
                    
                    // Also update display name to match name for simplicity
                    user.setDisplayName(newName);
                    
                    // Update in Firebase Auth as well
                    try {
                        UserRecord.UpdateRequest updateRequest = new UserRecord.UpdateRequest(uid)
                            .setDisplayName(newName);
                        firebaseAuthService.getUserAuth().updateUser(updateRequest);
                        logger.debug("Firebase Auth display name updated successfully");
                    } catch (FirebaseAuthException e) {
                        logger.error("Failed to update display name in Firebase Auth: {}", e.getMessage(), e);
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                            "Error updating display name in authentication system");
                    }
                }
            }
            
            // Update notification preferences if provided
            if (request.getNotificationPreferences() != null && !request.getNotificationPreferences().isEmpty()) {
                Map<String, Boolean> currentPrefs = user.getNotificationPreferences();
                
                // Initialize if null
                if (currentPrefs == null) {
                    currentPrefs = request.getNotificationPreferences();
                    user.setNotificationPreferences(currentPrefs);
                    logger.debug("Initialized notification preferences: {}", currentPrefs);
                } else {
                    // Update existing preferences
                    currentPrefs.putAll(request.getNotificationPreferences());
                    logger.debug("Updated notification preferences: {}", currentPrefs);
                }
            }
            
            // Save updated user
            logger.info("Profile updated successfully for user: {}", uid);
            return userRepository.save(user);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating user profile: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating user profile: " + e.getMessage());
        }
    }

    public User updateNotificationPreferences(String uid, Map<String, Boolean> notificationPreferences) {
        try {
            logger.info("Updating notification preferences for user: {}", uid);
            
            // Validate input
            if (notificationPreferences == null || notificationPreferences.isEmpty()) {
                logger.warn("Empty notification preferences provided for user: {}", uid);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Notification preferences cannot be empty");
            }
            
            // Validate if user exists
            User user = userRepository.findById(uid)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.USER_NOT_FOUND));
            
            // Get current preferences or initialize if null
            Map<String, Boolean> currentPrefs = user.getNotificationPreferences();
            if (currentPrefs == null) {
                currentPrefs = new HashMap<>();
                user.setNotificationPreferences(currentPrefs);
            }
            
            // Track if any changes were made
            boolean changes = false;
            
            // Update notification preferences
            for (Map.Entry<String, Boolean> entry : notificationPreferences.entrySet()) {
                String key = entry.getKey();
                Boolean newValue = entry.getValue();
                Boolean oldValue = currentPrefs.get(key);
                
                // Only update if value is changing
                if (newValue != null && !newValue.equals(oldValue)) {
                    currentPrefs.put(key, newValue);
                    logger.debug("Updated preference '{}' from '{}' to '{}'", key, oldValue, newValue);
                    changes = true;
                }
            }
            
            // Only save if changes were made
            if (changes) {
                logger.info("Notification preferences updated successfully for user: {}", uid);
                return userRepository.save(user);
            } else {
                logger.info("No changes to notification preferences for user: {}", uid);
                return user;
            }
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating notification preferences: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error updating notification preferences: " + e.getMessage());
        }
    }

    public void deleteUserAccount(String uid) {
        try {
            logger.info("Deleting account for user: {}", uid);
            // Check if user exists
            if (!userRepository.existsById(uid)) {
                throw new ResourceNotFoundException(AppConstants.USER_NOT_FOUND);
            }
            
            // Delete from Firebase Auth
            try {
                firebaseAuthService.getUserAuth().deleteUser(uid);
            } catch (FirebaseAuthException e) {
                logger.error("Failed to delete user from Firebase Auth: {}", e.getMessage(), e);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error deleting user from authentication system: " + e.getMessage());
            }
            
            // Delete from Firestore
            userRepository.deleteById(uid);
            logger.info("User account deleted successfully: {}", uid);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting user account: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting user account: " + e.getMessage());
        }
    }

    public void updateUserProfile(String uid, String displayName) {
        try {
            logger.info("Updating display name for user: {}", uid);
            // Update in Firebase Auth
            firebaseAuthService.updateUserProfile(uid, displayName);

            // Update in Firestore
            userRepository.findById(uid).ifPresent(user -> {
                user.setDisplayName(displayName);
                user.setName(displayName); // Also update name to keep them in sync
                userRepository.save(user);
            });
            logger.info("Display name updated successfully for user: {}", uid);
        } catch (FirebaseAuthException e) {
            logger.error("Failed to update user profile in Firebase Auth: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating user profile: " + e.getMessage());
        }
    }
}