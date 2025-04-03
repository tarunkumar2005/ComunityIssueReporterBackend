package com.fixit.FixIt.repository;

import com.fixit.FixIt.model.UserUpvote;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class UserUpvoteRepository {
    private static final String COLLECTION_NAME = "user_upvotes";

    private Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }

    public UserUpvote save(UserUpvote userUpvote) {
        try {
            // Generate a new document with auto-generated ID if not set
            if (userUpvote.getId() == null || userUpvote.getId().isEmpty()) {
                DocumentReference docRef = getFirestore().collection(COLLECTION_NAME).document();
                userUpvote.setId(docRef.getId());
            }

            getFirestore().collection(COLLECTION_NAME)
                .document(userUpvote.getId())
                .set(userUpvote.toMap())
                .get(); // Wait for operation to complete
            return userUpvote;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error saving user upvote to Firestore", e);
        }
    }

    public boolean hasUserUpvotedIssue(String userId, String issueId) {
        try {
            QuerySnapshot querySnapshot = getFirestore().collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .whereEqualTo("issueId", issueId)
                .limit(1)
                .get()
                .get();

            return !querySnapshot.isEmpty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error checking if user has upvoted issue", e);
        }
    }

    public Optional<UserUpvote> findByUserIdAndIssueId(String userId, String issueId) {
        try {
            QuerySnapshot querySnapshot = getFirestore().collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .whereEqualTo("issueId", issueId)
                .limit(1)
                .get()
                .get();

            if (!querySnapshot.isEmpty()) {
                DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                return Optional.of(UserUpvote.fromMap(document.getData()));
            }
            return Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding user upvote", e);
        }
    }
    
    public void deleteByIssueId(String issueId) {
        try {
            // Find all upvotes for this issue
            QuerySnapshot querySnapshot = getFirestore().collection(COLLECTION_NAME)
                .whereEqualTo("issueId", issueId)
                .get()
                .get();
            
            // Delete each upvote
            WriteBatch batch = getFirestore().batch();
            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                batch.delete(document.getReference());
            }
            
            // Commit the batch
            if (!querySnapshot.isEmpty()) {
                batch.commit().get();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error deleting upvotes for issue", e);
        }
    }
}