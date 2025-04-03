package com.fixit.FixIt.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;

/**
 * Configuration class for loading and validating environment variables.
 * This ensures that all required environment variables are available.
 */
@Configuration
public class EnvironmentConfig {
    private static final Logger logger = LoggerFactory.getLogger(EnvironmentConfig.class);
    
    private final Environment environment;
    
    @Autowired
    public EnvironmentConfig(Environment environment) {
        this.environment = environment;
    }
    
    @PostConstruct
    public void validateEnvironmentVariables() {
        logger.info("Validating environment variables...");
        
        // Check Firebase variables
        checkEnvVariable("FIREBASE_PROJECT_ID", "firebase.project-id");
        checkEnvVariable("FIREBASE_STORAGE_BUCKET", "firebase.storage-bucket");
        checkEnvVariable("FIREBASE_API_KEY", "firebase.api-key");
        
        // Check JWT variables
        checkEnvVariable("JWT_EXPIRATION", "jwt.expiration");
        checkEnvVariable("JWT_REFRESH_EXPIRATION", "jwt.refresh-expiration");
        
        logger.info("Environment validation complete");
    }
    
    private void checkEnvVariable(String envVar, String propertyName) {
        String value = environment.getProperty(propertyName);
        if (value != null) {
            // Check if we're using a default value (which means env var wasn't provided)
            String environmentValue = System.getenv(envVar);
            if (environmentValue == null) {
                logger.warn("Environment variable {} not set, using default value", envVar);
            } else {
                logger.info("Environment variable {} is properly configured", envVar);
            }
        } else {
            logger.error("Missing required property: {}", propertyName);
        }
    }
} 