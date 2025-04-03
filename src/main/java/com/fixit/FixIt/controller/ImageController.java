package com.fixit.FixIt.controller;

import com.fixit.FixIt.dto.UploadImageRequest;
import com.fixit.FixIt.service.FirestoreImageService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
@CrossOrigin
public class ImageController {
    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);

    private final FirestoreImageService firestoreImageService;
    
    public ImageController(FirestoreImageService firestoreImageService) {
        this.firestoreImageService = firestoreImageService;
    }
    
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "Please select a file to upload"));
            }
            
            // Log file details
            logger.info("Uploading file: name={}, size={}, contentType={}", 
                file.getOriginalFilename(), file.getSize(), file.getContentType());
            
            String imageUrl = firestoreImageService.uploadImage(file);
            logger.info("Image uploaded successfully: {}", imageUrl);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", Map.of("imageUrl", imageUrl));
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ResponseStatusException e) {
            logger.error("Error during image upload: {}", e.getReason(), e);
            return ResponseEntity.status(e.getStatusCode())
                .body(Map.of("status", "error", "message", e.getReason()));
        } catch (Exception e) {
            logger.error("Unexpected error during image upload: {}", e.getMessage(), e);
            
            // Get detailed error information
            String errorDetails = "Unknown error";
            if (e.getCause() != null) {
                errorDetails = e.getCause().getMessage();
                logger.error("Cause: {}", errorDetails);
            }
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "error", 
                    "message", "Failed to upload image. Please try with a smaller image or different format.",
                    "details", e.getMessage(),
                    "cause", errorDetails
                ));
        }
    }
    
    @PostMapping("/upload/base64")
    public ResponseEntity<Map<String, Object>> uploadBase64Image(@RequestBody Map<String, String> request) {
        try {
            String base64Image = request.get("image");
            
            if (base64Image == null || base64Image.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "Base64 image is required"));
            }
            
            // Log base64 image length
            logger.info("Uploading base64 image, length={}", base64Image.length());
            
            String imageUrl = firestoreImageService.uploadBase64Image(base64Image);
            logger.info("Base64 image uploaded successfully: {}", imageUrl);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", Map.of("imageUrl", imageUrl));
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ResponseStatusException e) {
            logger.error("Error during base64 image upload: {}", e.getReason());
            return ResponseEntity.status(e.getStatusCode())
                .body(Map.of("status", "error", "message", e.getReason()));
        } catch (Exception e) {
            logger.error("Unexpected error during base64 image upload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", "error", "message", "Failed to upload image. Please try with a smaller image or different format."));
        }
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> addImageToIssue(@Valid @RequestBody UploadImageRequest request) {
        try {
            String imageUrl = firestoreImageService.addImageToIssue(request);
            logger.info("Image added to issue: {}", request.getIssueId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", Map.of("imageUrl", imageUrl));
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ResponseStatusException e) {
            logger.error("Error adding image to issue: {}", e.getReason());
            return ResponseEntity.status(e.getStatusCode())
                .body(Map.of("status", "error", "message", e.getReason()));
        } catch (Exception e) {
            logger.error("Unexpected error adding image to issue", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", "error", "message", "Failed to add image to issue"));
        }
    }
    
    @GetMapping("/{imageId}")
    public ResponseEntity<byte[]> getImage(@PathVariable String imageId) {
        try {
            Map<String, Object> imageData = firestoreImageService.getImage(imageId);
            logger.info("Image retrieved: {}", imageId);
            
            // Get image data and content type
            String base64Data = (String) imageData.get("data");
            String contentType = (String) imageData.get("contentType");
            
            // Decode base64 data
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            
            // Set appropriate headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            logger.error("Error retrieving image: {}", e.getReason());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error retrieving image", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to get image");
        }
    }
} 