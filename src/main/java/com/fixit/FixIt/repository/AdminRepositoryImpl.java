package com.fixit.FixIt.repository;

import com.fixit.FixIt.model.Admin;
import com.fixit.FixIt.model.IssueStatus;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Repository
public class AdminRepositoryImpl implements AdminRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminRepositoryImpl.class);
    private static final String COLLECTION_NAME = "admins";
    
    @Override
    public Admin save(Admin admin) {
        try {
            Firestore firestore = FirestoreClient.getFirestore();
            
            DocumentReference docRef;
            if (admin.getUid() != null) {
                docRef = firestore.collection(COLLECTION_NAME).document(admin.getUid());
            } else {
                docRef = firestore.collection(COLLECTION_NAME).document();
                admin.setUid(docRef.getId());
            }
            
            ApiFuture<WriteResult> result = docRef.set(admin);
            result.get(); // Wait for operation to complete
            
            return admin;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error saving admin: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to save admin: " + e.getMessage());
        }
    }

    @Override
    public Optional<Admin> findByUid(String uid) {
        try {
            Firestore firestore = FirestoreClient.getFirestore();
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(uid);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();
            
            if (document.exists()) {
                Admin admin = document.toObject(Admin.class);
                return Optional.ofNullable(admin);
            } else {
                return Optional.empty();
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error finding admin by uid: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to find admin: " + e.getMessage());
        }
    }

    @Override
    public Optional<Admin> findByEmail(String email) {
        try {
            Firestore firestore = FirestoreClient.getFirestore();
            CollectionReference adminsRef = firestore.collection(COLLECTION_NAME);
            Query query = adminsRef.whereEqualTo("email", email);
            ApiFuture<QuerySnapshot> future = query.get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            
            if (!documents.isEmpty()) {
                Admin admin = documents.get(0).toObject(Admin.class);
                return Optional.ofNullable(admin);
            } else {
                return Optional.empty();
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error finding admin by email: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to find admin: " + e.getMessage());
        }
    }

    @Override
    public boolean existsByUid(String uid) {
        try {
            return findByUid(uid).isPresent();
        } catch (Exception e) {
            logger.error("Error checking if admin exists: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to check if admin exists: " + e.getMessage());
        }
    }

    @Override
    public Admin updateStats(String adminUid, IssueStatus previousStatus, IssueStatus newStatus) {
        try {
            Optional<Admin> adminOpt = findByUid(adminUid);
            if (!adminOpt.isPresent()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Admin not found with uid: " + adminUid);
            }
            
            Admin admin = adminOpt.get();
            admin.updateStats(previousStatus, newStatus);
            
            return save(admin);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating admin stats: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to update admin stats: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getAnalytics(String startDate, String endDate) {
        // This would normally query the database for analytics data
        // For now, we'll return some dummy data
        Map<String, Object> analytics = new HashMap<>();
        
        analytics.put("totalAdmins", 5);
        analytics.put("totalIssuesResolved", 120);
        analytics.put("totalIssuesClosed", 30);
        analytics.put("totalIssuesRejected", 15);
        analytics.put("averageApprovalRate", 72.7);
        
        // Add some dummy time-series data
        List<Map<String, Object>> issuesByDay = new ArrayList<>();
        
        Map<String, Object> day1 = new HashMap<>();
        day1.put("date", "2025-04-01");
        day1.put("resolved", 12);
        day1.put("closed", 3);
        day1.put("rejected", 1);
        
        Map<String, Object> day2 = new HashMap<>();
        day2.put("date", "2025-04-02");
        day2.put("resolved", 15);
        day2.put("closed", 4);
        day2.put("rejected", 2);
        
        issuesByDay.add(day1);
        issuesByDay.add(day2);
        
        analytics.put("issuesByDay", issuesByDay);
        
        return analytics;
    }

    @Override
    public List<Admin> findAll() {
        try {
            Firestore firestore = FirestoreClient.getFirestore();
            CollectionReference adminsRef = firestore.collection(COLLECTION_NAME);
            ApiFuture<QuerySnapshot> future = adminsRef.get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            
            List<Admin> admins = new ArrayList<>();
            for (DocumentSnapshot document : documents) {
                Admin admin = document.toObject(Admin.class);
                if (admin != null) {
                    admins.add(admin);
                }
            }
            
            return admins;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error finding all admins: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to find all admins: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteByUid(String uid) {
        try {
            Firestore firestore = FirestoreClient.getFirestore();
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(uid);
            ApiFuture<DocumentSnapshot> getFuture = docRef.get();
            DocumentSnapshot document = getFuture.get();
            
            if (document.exists()) {
                ApiFuture<WriteResult> writeFuture = docRef.delete();
                writeFuture.get(); // Wait for operation to complete
                return true;
            } else {
                return false;
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error deleting admin: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to delete admin: " + e.getMessage());
        }
    }
} 