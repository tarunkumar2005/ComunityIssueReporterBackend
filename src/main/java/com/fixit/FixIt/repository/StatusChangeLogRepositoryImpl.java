package com.fixit.FixIt.repository;

import com.fixit.FixIt.model.StatusChangeLog;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class StatusChangeLogRepositoryImpl implements StatusChangeLogRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(StatusChangeLogRepositoryImpl.class);
    private static final String COLLECTION_NAME = "statusChangeLogs";
    
    @Override
    public StatusChangeLog save(StatusChangeLog log) {
        try {
            Firestore firestore = FirestoreClient.getFirestore();
            
            DocumentReference docRef;
            if (log.getId() != null && !log.getId().isEmpty()) {
                docRef = firestore.collection(COLLECTION_NAME).document(log.getId());
            } else {
                docRef = firestore.collection(COLLECTION_NAME).document();
                log.setId(docRef.getId());
            }
            
            ApiFuture<WriteResult> result = docRef.set(log);
            result.get(); // Wait for operation to complete
            
            return log;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error saving status change log: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to save status change log: " + e.getMessage());
        }
    }

    @Override
    public Optional<StatusChangeLog> findById(String id) {
        try {
            Firestore firestore = FirestoreClient.getFirestore();
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();
            
            if (document.exists()) {
                StatusChangeLog log = document.toObject(StatusChangeLog.class);
                return Optional.ofNullable(log);
            } else {
                return Optional.empty();
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error finding status change log by id: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to find status change log: " + e.getMessage());
        }
    }

    @Override
    public List<StatusChangeLog> findByIssueId(String issueId) {
        try {
            Firestore firestore = FirestoreClient.getFirestore();
            CollectionReference logsRef = firestore.collection(COLLECTION_NAME);
            Query query = logsRef.whereEqualTo("issueId", issueId);
            ApiFuture<QuerySnapshot> future = query.get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            
            List<StatusChangeLog> logs = new ArrayList<>();
            for (DocumentSnapshot document : documents) {
                StatusChangeLog log = document.toObject(StatusChangeLog.class);
                if (log != null) {
                    logs.add(log);
                }
            }
            
            return logs;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error finding status change logs by issueId: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to find status change logs: " + e.getMessage());
        }
    }

    @Override
    public List<StatusChangeLog> findByChangedByAdminUid(String adminUid) {
        try {
            Firestore firestore = FirestoreClient.getFirestore();
            CollectionReference logsRef = firestore.collection(COLLECTION_NAME);
            Query query = logsRef.whereEqualTo("changedByAdminUid", adminUid);
            ApiFuture<QuerySnapshot> future = query.get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            
            List<StatusChangeLog> logs = new ArrayList<>();
            for (DocumentSnapshot document : documents) {
                StatusChangeLog log = document.toObject(StatusChangeLog.class);
                if (log != null) {
                    logs.add(log);
                }
            }
            
            return logs;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error finding status change logs by adminUid: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to find status change logs: " + e.getMessage());
        }
    }

    @Override
    public List<StatusChangeLog> findByChangedByAdminUidAndChangedAtAfter(String adminUid, Date date) {
        try {
            Firestore firestore = FirestoreClient.getFirestore();
            CollectionReference logsRef = firestore.collection(COLLECTION_NAME);
            
            // Query for logs by the admin and after the specified date
            Query query = logsRef
                .whereEqualTo("changedByAdminUid", adminUid)
                .whereGreaterThanOrEqualTo("changedAt", date);
                
            ApiFuture<QuerySnapshot> future = query.get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            
            List<StatusChangeLog> logs = new ArrayList<>();
            for (DocumentSnapshot document : documents) {
                StatusChangeLog log = document.toObject(StatusChangeLog.class);
                if (log != null) {
                    logs.add(log);
                }
            }
            
            return logs;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error finding recent status change logs by adminUid: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to find recent status change logs: " + e.getMessage());
        }
    }

    @Override
    public int deleteByIssueId(String issueId) {
        try {
            Firestore firestore = FirestoreClient.getFirestore();
            CollectionReference logsRef = firestore.collection(COLLECTION_NAME);
            Query query = logsRef.whereEqualTo("issueId", issueId);
            ApiFuture<QuerySnapshot> future = query.get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            
            int count = 0;
            for (DocumentSnapshot document : documents) {
                ApiFuture<WriteResult> writeResult = document.getReference().delete();
                writeResult.get(); // Wait for operation to complete
                count++;
            }
            
            return count;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error deleting status change logs by issueId: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to delete status change logs: " + e.getMessage());
        }
    }
} 