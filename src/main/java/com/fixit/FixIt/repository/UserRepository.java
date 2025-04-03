package com.fixit.FixIt.repository;

import com.fixit.FixIt.model.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class UserRepository {
    private static final String COLLECTION_NAME = "users";

    private Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }

    public User save(User user) {
        try {
            getFirestore().collection(COLLECTION_NAME)
                .document(user.getUid())
                .set(user.toMap())
                .get(); // Wait for operation to complete
            return user;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error saving user to Firestore", e);
        }
    }

    public Optional<User> findByUsername(String username) {
        try {
            QuerySnapshot querySnapshot = getFirestore().collection(COLLECTION_NAME)
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .get();

            if (!querySnapshot.isEmpty()) {
                DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                return Optional.of(User.fromMap(document.getData()));
            }
            return Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding user by username", e);
        }
    }

    public Optional<User> findByEmail(String email) {
        try {
            QuerySnapshot querySnapshot = getFirestore().collection(COLLECTION_NAME)
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .get();

            if (!querySnapshot.isEmpty()) {
                DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                return Optional.of(User.fromMap(document.getData()));
            }
            return Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding user by email", e);
        }
    }

    public Optional<User> findById(String uid) {
        try {
            DocumentSnapshot document = getFirestore().collection(COLLECTION_NAME)
                .document(uid)
                .get()
                .get(); // Wait for operation to complete

            if (document.exists()) {
                return Optional.of(User.fromMap(document.getData()));
            }
            return Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding user by ID", e);
        }
    }

    public void deleteById(String uid) {
        try {
            getFirestore().collection(COLLECTION_NAME)
                .document(uid)
                .delete()
                .get(); // Wait for operation to complete
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error deleting user", e);
        }
    }

    public boolean existsById(String uid) {
        try {
            DocumentSnapshot document = getFirestore().collection(COLLECTION_NAME)
                .document(uid)
                .get()
                .get(); // Wait for operation to complete

            return document.exists();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error checking if user exists", e);
        }
    }
}