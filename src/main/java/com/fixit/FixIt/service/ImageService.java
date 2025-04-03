package com.fixit.FixIt.service;

import com.fixit.FixIt.dto.UploadImageRequest;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Iterator;
import java.util.UUID;

@Service
public class ImageService {
    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);
    private static final int MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024; // 5MB
    private static final float IMAGE_COMPRESSION_QUALITY = 0.7f; // 70% quality

    @Value("${firebase.storage-bucket}")
    private String storageBucket;
    
    private final IssueService issueService;
    
    public ImageService(IssueService issueService) {
        this.issueService = issueService;
    }
    
    public String uploadImage(MultipartFile file) {
        try {
            // Check file size
            if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
                logger.info("Image too large ({}), compressing...", file.getSize());
                byte[] compressedImageData = compressImage(file.getBytes(), getImageFormat(file.getContentType()));
                return uploadImageBytes(compressedImageData, getImageFormat(file.getContentType()));
            }
            
            // Generate a unique file name
            String fileName = "issues/" + UUID.randomUUID().toString() + "." + getImageFormat(file.getContentType());
            
            // Determine the content type
            String contentType = file.getContentType();
            if (contentType == null) {
                contentType = "image/jpeg";
            }
            
            // Create blob info
            BlobId blobId = BlobId.of(storageBucket, fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(contentType)
                .build();
            
            // Upload the file
            Storage storage = StorageClient.getInstance().bucket().getStorage();
            try {
                Blob blob = storage.create(blobInfo, file.getBytes());
                
                // Get the public URL
                return "https://storage.googleapis.com/" + storageBucket + "/" + fileName;
            } catch (Exception e) {
                logger.error("Error uploading to Firebase Storage: {}", e.getMessage());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Failed to upload to storage: " + e.getMessage());
            }
        } catch (IOException e) {
            logger.error("IO error during image upload: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to process image: " + e.getMessage());
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during image upload: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to upload image: " + e.getMessage());
        }
    }
    
    public String uploadBase64Image(String base64Image) {
        try {
            // Remove data URL prefix if present
            String imageContent = base64Image;
            String contentType = "image/jpeg"; // Default content type
            
            if (base64Image.startsWith("data:")) {
                String[] parts = base64Image.split(",");
                if (parts.length > 1) {
                    contentType = parts[0].split(";")[0].split(":")[1];
                    imageContent = parts[1];
                }
            }
            
            // Decode base64 content
            byte[] imageBytes = Base64.getDecoder().decode(imageContent);
            
            // Check if compression is needed
            if (imageBytes.length > MAX_IMAGE_SIZE_BYTES) {
                logger.info("Base64 image too large ({}), compressing...", imageBytes.length);
                imageBytes = compressImage(imageBytes, getImageFormat(contentType));
            }
            
            return uploadImageBytes(imageBytes, getImageFormat(contentType));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid base64 format: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Invalid base64 image format: " + e.getMessage());
        } catch (IOException e) {
            logger.error("IO error during base64 image processing: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to process image: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error uploading base64 image: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to upload image: " + e.getMessage());
        }
    }
    
    private String uploadImageBytes(byte[] imageBytes, String format) {
        try {
            // Generate a unique file name
            String fileName = "issues/" + UUID.randomUUID().toString() + "." + format;
            
            // Create blob info
            BlobId blobId = BlobId.of(storageBucket, fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType("image/" + format)
                .build();
            
            // Upload the file
            Storage storage = StorageClient.getInstance().bucket().getStorage();
            Blob blob = storage.create(blobInfo, imageBytes);
            
            // Get the public URL
            return "https://storage.googleapis.com/" + storageBucket + "/" + fileName;
        } catch (Exception e) {
            logger.error("Error uploading to Firebase Storage: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to upload to storage: " + e.getMessage());
        }
    }
    
    private byte[] compressImage(byte[] imageData, String format) throws IOException {
        // Read the image
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
        if (image == null) {
            logger.error("Cannot read image data for compression");
            throw new IOException("Cannot read image data");
        }
        
        // Create output stream for the compressed image
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        // Get appropriate image writer
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
        if (!writers.hasNext()) {
            logger.error("No writer found for format: {}", format);
            throw new IOException("Unsupported image format: " + format);
        }
        
        ImageWriter writer = writers.next();
        ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream);
        writer.setOutput(imageOutputStream);
        
        // Set compression quality
        ImageWriteParam param = writer.getDefaultWriteParam();
        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(IMAGE_COMPRESSION_QUALITY);
        }
        
        // Write the image
        writer.write(null, new IIOImage(image, null, null), param);
        
        // Cleanup
        writer.dispose();
        imageOutputStream.close();
        
        return outputStream.toByteArray();
    }
    
    private String getImageFormat(String contentType) {
        if (contentType == null) return "jpg";
        
        switch (contentType.toLowerCase()) {
            case "image/png":
                return "png";
            case "image/gif":
                return "gif";
            case "image/bmp":
                return "bmp";
            case "image/webp":
                return "webp";
            case "image/jpeg":
            case "image/jpg":
            default:
                return "jpg";
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
            logger.error("Error adding image to issue: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to add image to issue: " + e.getMessage());
        }
    }
    
    public byte[] getImage(String imageName) {
        try {
            // Get the Storage instance
            Storage storage = StorageClient.getInstance().bucket().getStorage();
            
            // Get the blob
            Blob blob = storage.get(BlobId.of(storageBucket, imageName));
            
            if (blob == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found");
            }
            
            return blob.getContent();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving image: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to get image: " + e.getMessage());
        }
    }

    // Simple direct upload method for testing
    public String uploadImageDirect(MultipartFile file) {
        try {
            // Generate a unique file name - use jpg extension always for testing
            String fileName = "issues/test_" + UUID.randomUUID().toString() + ".jpg";
            
            // Create blob info
            BlobId blobId = BlobId.of(storageBucket, fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType("image/jpeg")
                .build();
            
            // Log upload attempt
            logger.info("Starting direct upload to {}", fileName);
            
            // Upload the file
            Storage storage = StorageClient.getInstance().bucket().getStorage();
            Blob blob = storage.create(blobInfo, file.getBytes());
            
            // Log successful upload
            logger.info("Direct upload successful: {}", fileName);
            
            // Get the public URL
            return "https://storage.googleapis.com/" + storageBucket + "/" + fileName;
        } catch (Exception e) {
            // Log full exception details
            logger.error("Direct upload failed", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Direct upload failed: " + e.getMessage());
        }
    }
} 