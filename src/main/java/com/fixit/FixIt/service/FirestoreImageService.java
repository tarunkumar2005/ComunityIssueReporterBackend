package com.fixit.FixIt.service;

import com.fixit.FixIt.dto.UploadImageRequest;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class FirestoreImageService {
    private static final Logger logger = LoggerFactory.getLogger(FirestoreImageService.class);
    private static final String COLLECTION_NAME = "images";
    
    private final IssueService issueService;
    
    public FirestoreImageService(IssueService issueService) {
        this.issueService = issueService;
    }
    
    public String uploadImage(MultipartFile file) {
        try {
            // Convert MultipartFile to Base64
            byte[] fileContent = file.getBytes();
            String base64Content = Base64.getEncoder().encodeToString(fileContent);
            
            // Add metadata
            String contentType = file.getContentType();
            if (contentType == null) {
                contentType = "image/jpeg";
            }
            
            // Store in Firestore
            return storeImageInFirestore(base64Content, contentType, file.getOriginalFilename());
        } catch (IOException e) {
            logger.error("Failed to process image file", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to process image: " + e.getMessage());
        }
    }
    
    public String uploadBase64Image(String base64Image) {
        try {
            // Handle data URL format
            String base64Content = base64Image;
            String contentType = "image/jpeg";
            
            if (base64Image.startsWith("data:")) {
                String[] parts = base64Image.split(",");
                if (parts.length > 1) {
                    contentType = parts[0].split(";")[0].split(":")[1];
                    base64Content = parts[1];
                }
            }
            
            // Store in Firestore
            return storeImageInFirestore(base64Content, contentType, null);
        } catch (Exception e) {
            logger.error("Failed to process base64 image", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to process image: " + e.getMessage());
        }
    }
    
    private String storeImageInFirestore(String base64Content, String contentType, String originalFilename) {
        try {
            Firestore firestore = FirestoreClient.getFirestore();
            
            // Generate unique ID
            String imageId = UUID.randomUUID().toString();
            
            // Create document with image data
            Map<String, Object> imageData = new HashMap<>();
            imageData.put("id", imageId);
            imageData.put("data", base64Content);
            imageData.put("contentType", contentType);
            imageData.put("createdAt", System.currentTimeMillis());
            
            if (originalFilename != null) {
                imageData.put("originalFilename", originalFilename);
            }
            
            // Save to Firestore
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(imageId);
            docRef.set(imageData).get();
            
            // Return URL-like identifier
            return "/api/images/" + imageId;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Failed to store image in Firestore", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to store image: " + e.getMessage());
        }
    }
    
    public String addImageToIssue(UploadImageRequest request) {
        try {
            String imageUrl = request.getImageUrl();
            String issueId = request.getIssueId();
            
            if (issueId != null && !issueId.isEmpty()) {
                // Add image to issue
                issueService.addImageToIssue(issueId, imageUrl);
            }
            
            return imageUrl;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to add image to issue", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to add image to issue: " + e.getMessage());
        }
    }
    
    public Map<String, Object> getImage(String imageId) {
        try {
            Firestore firestore = FirestoreClient.getFirestore();
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(imageId);
            
            Map<String, Object> imageData = docRef.get().get().getData();
            if (imageData == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found");
            }
            
            return imageData;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Failed to retrieve image from Firestore", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to retrieve image: " + e.getMessage());
        }
    }
} 