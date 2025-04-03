package com.fixit.FixIt.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    
    @Value("${firebase.project-id}")
    private String firebaseProjectId;
    
    @Value("${firebase.storage-bucket}")
    private String firebaseStorageBucket;
    
    @Value("${firebase.api-key}")
    private String firebaseApiKey;
    
    @Value("${jwt.expiration}")
    private String jwtExpiration;
    
    @Value("${jwt.refresh-expiration}")
    private String jwtRefreshExpiration;
    
    @Autowired
    public EnvironmentConfig(Environment environment) {
        this.environment = environment;
    }
    
    @PostConstruct
    public void validateEnvironmentVariables() {
        logger.info("Validating environment variables...");
        
        // Check Firebase variables
        logConfigValue("firebase.project-id", firebaseProjectId);
        logConfigValue("firebase.storage-bucket", firebaseStorageBucket);
        logConfigValue("firebase.api-key", firebaseApiKey);
        
        // Check JWT variables
        logConfigValue("jwt.expiration", jwtExpiration);
        logConfigValue("jwt.refresh-expiration", jwtRefreshExpiration);
        
        logger.info("Environment validation complete");
    }
    
    private void logConfigValue(String propertyName, String value) {
        if (value != null && !value.isEmpty()) {
            logger.info("Configuration property '{}' is set to: {}", propertyName, value);
        } else {
            logger.error("Missing required configuration property: {}", propertyName);
        }
    }
} 