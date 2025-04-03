package com.fixit.FixIt.util;

import com.fixit.FixIt.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

public final class ValidationUtil {
    private static final Logger logger = LoggerFactory.getLogger(ValidationUtil.class);
    
    // Regular expression patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,}$");
    
    private ValidationUtil() {
        // Private constructor to prevent instantiation
    }
    
    // String validations
    public static void validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            logger.warn("Validation failed: {} is required", fieldName);
            throw new BadRequestException(fieldName + " is required");
        }
    }
    
    // Object validation
    public static void validateRequired(Object value, String fieldName) {
        if (value == null) {
            logger.warn("Validation failed: {} is required", fieldName);
            throw new BadRequestException(fieldName + " is required");
        }
    }
    
    public static void validateMinLength(String value, String fieldName, int minLength) {
        validateRequired(value, fieldName);
        if (value.length() < minLength) {
            logger.warn("Validation failed: {} must be at least {} characters", fieldName, minLength);
            throw new BadRequestException(fieldName + " must be at least " + minLength + " characters");
        }
    }
    
    public static void validateMaxLength(String value, String fieldName, int maxLength) {
        validateRequired(value, fieldName);
        if (value.length() > maxLength) {
            logger.warn("Validation failed: {} must be less than {} characters", fieldName, maxLength);
            throw new BadRequestException(fieldName + " must be less than " + maxLength + " characters");
        }
    }
    
    public static void validateEmail(String email) {
        validateRequired(email, "Email");
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            logger.warn("Validation failed: Invalid email format");
            throw new BadRequestException("Please enter a valid email address");
        }
    }
    
    public static void validateUsername(String username) {
        validateRequired(username, "Username");
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            logger.warn("Validation failed: Invalid username format");
            throw new BadRequestException("Username can only contain letters, numbers, and underscores, and must be 3-20 characters");
        }
    }
    
    public static void validatePassword(String password) {
        validateRequired(password, "Password");
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            logger.warn("Validation failed: Password doesn't meet security requirements");
            throw new BadRequestException("Password must be at least 6 characters and include at least one uppercase letter, one lowercase letter, and one number");
        }
    }
    
    // Collection validations
    public static <T> void validateNotEmpty(Collection<T> collection, String fieldName) {
        if (collection == null || collection.isEmpty()) {
            logger.warn("Validation failed: {} cannot be empty", fieldName);
            throw new BadRequestException(fieldName + " cannot be empty");
        }
    }
    
    public static <K, V> void validateNotEmpty(Map<K, V> map, String fieldName) {
        if (map == null || map.isEmpty()) {
            logger.warn("Validation failed: {} cannot be empty", fieldName);
            throw new BadRequestException(fieldName + " cannot be empty");
        }
    }
    
    // Numeric validations
    public static void validatePositive(Number value, String fieldName) {
        if (value == null || value.doubleValue() <= 0) {
            logger.warn("Validation failed: {} must be a positive number", fieldName);
            throw new BadRequestException(fieldName + " must be a positive number");
        }
    }
    
    public static void validateRange(Number value, String fieldName, Number min, Number max) {
        if (value == null || value.doubleValue() < min.doubleValue() || value.doubleValue() > max.doubleValue()) {
            logger.warn("Validation failed: {} must be between {} and {}", fieldName, min, max);
            throw new BadRequestException(fieldName + " must be between " + min + " and " + max);
        }
    }
} 