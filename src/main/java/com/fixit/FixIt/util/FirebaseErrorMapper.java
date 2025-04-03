package com.fixit.FixIt.util;

import com.fixit.FixIt.exception.BadRequestException;
import com.fixit.FixIt.exception.ResourceNotFoundException;
import com.fixit.FixIt.exception.UnauthorizedException;
import com.google.firebase.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FirebaseErrorMapper {
    
    private static final Logger logger = LoggerFactory.getLogger(FirebaseErrorMapper.class);
    
    private FirebaseErrorMapper() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Map Firebase ErrorCode to a user-friendly error message
     * @param errorCode Firebase error code
     * @return User-friendly error message
     */
    public static String getFirebaseErrorMessage(ErrorCode errorCode) {
        if (errorCode == null) {
            return AppConstants.SERVER_ERROR;
        }
        
        switch (errorCode) {
            case ALREADY_EXISTS:
                return "This email is already registered. Please use a different email or try logging in.";
            case INVALID_ARGUMENT:
                return "Invalid email or password format. Please check your input.";
            case PERMISSION_DENIED:
                return "Access denied. Please check your credentials.";
            case UNAUTHENTICATED:
                return "Please sign in to continue.";
            case NOT_FOUND:
                return "No account found with this email. Please check your email or sign up.";
            case FAILED_PRECONDITION:
                return "Unable to complete the request. Please check your input.";
            case INTERNAL:
                return AppConstants.SERVER_ERROR;
            case UNAVAILABLE:
                return "Service is currently unavailable. Please try again later.";
            default:
                return AppConstants.SERVER_ERROR;
        }
    }
    
    /**
     * Map Firebase REST API error codes to user-friendly error messages
     * @param errorCode Firebase REST API error code
     * @return User-friendly error message
     */
    public static String getFirebaseRestErrorMessage(String errorCode) {
        if (errorCode == null) {
            return AppConstants.SERVER_ERROR;
        }
        
        switch (errorCode) {
            case "EMAIL_EXISTS":
                return "This email is already registered. Please use a different email or try logging in.";
            case "INVALID_LOGIN_CREDENTIALS":
                return "Invalid email/username or password. Please try again.";
            case "INVALID_PASSWORD":
                return "The password is incorrect. Please try again.";
            case "EMAIL_NOT_FOUND":
                return "No account found with this email. Please check your email or sign up.";
            case "WEAK_PASSWORD":
                return "Password should be at least 6 characters long.";
            case "INVALID_EMAIL":
                return "Please enter a valid email address.";
            case "USER_DISABLED":
                return "This account has been disabled. Please contact support.";
            case "TOO_MANY_ATTEMPTS_TRY_LATER":
                return "Too many unsuccessful login attempts. Please try again later.";
            case "OPERATION_NOT_ALLOWED":
                return "This operation is not allowed. Please contact support.";
            default:
                return AppConstants.SERVER_ERROR;
        }
    }
    
    /**
     * Throws appropriate exception based on Firebase ErrorCode
     * @param errorCode Firebase error code
     * @param defaultMessage Default error message if no specific mapping exists
     */
    public static void throwMappedException(ErrorCode errorCode, String defaultMessage) {
        String message = getFirebaseErrorMessage(errorCode);
        logger.error("Firebase error: {} ({})", message, errorCode);
        
        switch (errorCode) {
            case NOT_FOUND:
                throw new ResourceNotFoundException(message);
            case UNAUTHENTICATED:
            case PERMISSION_DENIED:
                throw new UnauthorizedException(message);
            default:
                throw new BadRequestException(defaultMessage != null ? defaultMessage : message);
        }
    }
    
    /**
     * Throws appropriate exception based on Firebase REST API error code
     * @param errorCode Firebase REST API error code
     * @param defaultMessage Default error message if no specific mapping exists
     */
    public static void throwMappedException(String errorCode, String defaultMessage) {
        String message = getFirebaseRestErrorMessage(errorCode);
        logger.error("Firebase error: {} ({})", message, errorCode);
        
        switch (errorCode) {
            case "EMAIL_NOT_FOUND":
                throw new ResourceNotFoundException(message);
            case "USER_DISABLED":
            case "INVALID_PASSWORD":
                throw new UnauthorizedException(message);
            default:
                throw new BadRequestException(defaultMessage != null ? defaultMessage : message);
        }
    }
} 