package com.fixit.FixIt.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.project-id}")
    private String projectId;
    
    @Value("${firebase.storage-bucket}")
    private String storageBucket;
    
    private final ResourceLoader resourceLoader;
    
    public FirebaseConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
    
    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        logger.info("Initializing Firebase application with projectId: {}", projectId);
        
        try {
            // Check if already initialized
            if (FirebaseApp.getApps().isEmpty()) {
                // Try multiple approaches to load the service account file
                InputStream serviceAccount = getServiceAccountStream();
                
                if (serviceAccount == null) {
                    throw new IOException("Could not load firebase-service-account.json from any location");
                }
                
                logger.info("Successfully loaded Firebase service account credentials");
                
                FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setProjectId(projectId)
                    .setStorageBucket(storageBucket)
                    .build();
                
                logger.info("Firebase options configured successfully");
                return FirebaseApp.initializeApp(options);
            } else {
                logger.info("Firebase App already initialized");
                return FirebaseApp.getInstance();
            }
        } catch (Exception e) {
            logger.error("Firebase initialization error: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private InputStream getServiceAccountStream() {
        logger.debug("Attempting to load firebase-service-account.json");
        InputStream serviceAccount = null;
        
        // Approach 1: Try loading from classpath resources
        try {
            Resource resource = new ClassPathResource("firebase-service-account.json");
            if (resource.exists()) {
                logger.debug("Found service account file in classpath");
                return resource.getInputStream();
            }
        } catch (Exception e) {
            logger.warn("Could not load service account from classpath: {}", e.getMessage());
        }
        
        // Approach 2: Try loading from file system (absolute path)
        try {
            File file = new File("src/main/resources/firebase-service-account.json");
            if (file.exists()) {
                logger.debug("Found service account file at: {}", file.getAbsolutePath());
                return new FileInputStream(file);
            }
        } catch (Exception e) {
            logger.warn("Could not load service account from file system: {}", e.getMessage());
        }
        
        // Approach 3: Try loading using ResourceLoader
        try {
            Resource resource = resourceLoader.getResource("classpath:firebase-service-account.json");
            if (resource.exists()) {
                logger.debug("Found service account file using ResourceLoader");
                return resource.getInputStream();
            }
        } catch (Exception e) {
            logger.warn("Could not load service account using ResourceLoader: {}", e.getMessage());
        }
        
        logger.error("Could not find firebase-service-account.json in any location");
        return null;
    }
}