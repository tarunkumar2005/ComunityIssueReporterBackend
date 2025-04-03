package com.fixit.FixIt.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;
import com.google.firebase.auth.FirebaseToken;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.Date;

@Service
public class FirebaseAuthService {
    
    @Value("${firebase.api-key}")
    private String firebaseApiKey;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final String FIREBASE_AUTH_BASE_URL = "https://identitytoolkit.googleapis.com/v1/accounts";

    public FirebaseAuthService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    private String getFirebaseErrorMessage(ErrorCode errorCode) {
        if (errorCode == null) {
            return "An error occurred. Please try again later.";
        }
        
        switch (errorCode) {
            case ALREADY_EXISTS:
                return "This email is already registered. Please use a different email or try logging in.";
            case INVALID_ARGUMENT:
                return "Invalid email or password format. Please check your input.";
            case PERMISSION_DENIED:
                return "Access denied. Please check your credentials.";
            case UNAUTHENTICATED:
                return "Please sign in to continue.";
            case NOT_FOUND:
                return "No account found with this email. Please check your email or sign up.";
            case FAILED_PRECONDITION:
                return "Unable to complete the request. Please check your input.";
            case INTERNAL:
                return "An internal error occurred. Please try again later.";
            case UNAVAILABLE:
                return "Service is currently unavailable. Please try again later.";
            default:
                return "An error occurred. Please try again later.";
        }
    }

    private String getFirebaseRestErrorMessage(String errorCode) {
        switch (errorCode) {
            case "EMAIL_EXISTS":
                return "This email is already registered. Please use a different email or try logging in.";
            case "INVALID_LOGIN_CREDENTIALS":
                return "Invalid email/username or password. Please try again.";
            case "INVALID_PASSWORD":
                return "The password is incorrect. Please try again.";
            case "EMAIL_NOT_FOUND":
                return "No account found with this email. Please check your email or sign up.";
            case "WEAK_PASSWORD":
                return "Password should be at least 6 characters long.";
            case "INVALID_EMAIL":
                return "Please enter a valid email address.";
            default:
                return "An error occurred. Please try again later.";
        }
    }
    
    public UserRecord createUser(String email, String password, String name) throws FirebaseAuthException {
        try {
            System.out.println("Creating user with email: " + email + " and name: " + name);
            
            // Generate username from name
            String baseUsername = name.toLowerCase().replaceAll("[^a-z0-9]", "");
            String username = baseUsername;
            int counter = 1;
            
            // Check if username exists and generate a unique one if needed
            while (true) {
                System.out.println("Checking if username exists: " + username);
                QuerySnapshot usernameQuery = FirestoreClient.getFirestore()
                    .collection("users")
                    .whereEqualTo("username", username)
                    .get()
                    .get();

                if (usernameQuery.isEmpty()) {
                    System.out.println("Username is available: " + username);
                    break;
                }
                System.out.println("Username already exists, trying next: " + baseUsername + counter);
                username = baseUsername + counter++;
            }

            // Create the user in Firebase Auth
            System.out.println("Creating user in Firebase Auth");
            CreateRequest request = new CreateRequest()
                .setEmail(email)
                .setPassword(password)
                .setDisplayName(name)
                .setEmailVerified(false);
            
            UserRecord user = FirebaseAuth.getInstance().createUser(request);
            System.out.println("User created in Firebase Auth with UID: " + user.getUid());

            // Store additional user data in Firestore
            System.out.println("Storing user data in Firestore");
            Map<String, Object> userData = new HashMap<>();
            userData.put("uid", user.getUid());
            userData.put("email", email);
            userData.put("username", username);
            userData.put("displayName", name);
            userData.put("name", name);
            userData.put("role", "USER");
            userData.put("accountCreationDate", new Date());
            userData.put("lastLogin", new Date());
            
            // Initialize notification preferences
            Map<String, Boolean> notificationPrefs = new HashMap<>();
            notificationPrefs.put("ownIssues", true);
            notificationPrefs.put("communityActivity", false);
            userData.put("notificationPreferences", notificationPrefs);

            FirestoreClient.getFirestore()
                .collection("users")
                .document(user.getUid())
                .set(userData)
                .get();
            System.out.println("User data stored in Firestore");

            return user;
        } catch (FirebaseAuthException e) {
            System.err.println("FirebaseAuthException in createUser: " + e.getMessage());
            String errorMessage = getFirebaseErrorMessage(e.getErrorCode());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("InterruptedException or ExecutionException in createUser: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to create account at this time. Please try again later.");
        } catch (Exception e) {
            System.err.println("Unexpected exception in createUser: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred: " + e.getMessage());
        }
    }
    
    public Map<String, Object> signIn(String loginIdentifier, String password) throws FirebaseAuthException {
        try {
            String email;
            
            // Check if loginIdentifier is an email or username
            if (loginIdentifier.contains("@")) {
                email = loginIdentifier;
            } else {
                // Find user by username
                QuerySnapshot usernameQuery = FirestoreClient.getFirestore()
                    .collection("users")
                    .whereEqualTo("username", loginIdentifier)
                    .limit(1)
                    .get()
                    .get();

                if (usernameQuery.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No account found with this username.");
                }

                email = usernameQuery.getDocuments().get(0).getString("email");
            }

            // Authenticate with Firebase using email/password
            String signInUrl = String.format("%s:signInWithPassword?key=%s", FIREBASE_AUTH_BASE_URL, firebaseApiKey);
            
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("email", email);
            requestBody.put("password", password);
            requestBody.put("returnSecureToken", "true");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            try {
                ResponseEntity<Map> response = restTemplate.exchange(
                    signInUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers),
                    Map.class
                );

                // Get user data from Firestore
                String uid = (String) response.getBody().get("localId");
                DocumentSnapshot userData = FirestoreClient.getFirestore()
                    .collection("users")
                    .document(uid)
                    .get()
                    .get();

                if (!userData.exists()) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to retrieve user data. Please try again.");
                }

                // Prepare response
                Map<String, Object> userResponse = new HashMap<>(userData.getData());
                userResponse.put("token", response.getBody().get("idToken"));
                userResponse.put("uid", uid);  // Ensure UID is included
                
                // Ensure username is included
                if (userResponse.get("username") == null) {
                    System.err.println("Username is missing in user data from Firestore");
                    // Try to get username from displayName or email as fallback
                    String username = null;
                    
                    if (userResponse.get("name") != null) {
                        username = ((String) userResponse.get("name")).toLowerCase().replaceAll("[^a-z0-9]", "");
                    } else if (userResponse.get("displayName") != null) {
                        username = ((String) userResponse.get("displayName")).toLowerCase().replaceAll("[^a-z0-9]", "");
                    } else if (userResponse.get("email") != null) {
                        username = ((String) userResponse.get("email")).split("@")[0];
                    }
                    
                    if (username != null) {
                        userResponse.put("username", username);
                        // Update the username in Firestore
                        FirestoreClient.getFirestore()
                            .collection("users")
                            .document(uid)
                            .update("username", username)
                            .get();
                    }
                }

                // Update last login time
                FirestoreClient.getFirestore()
                    .collection("users")
                    .document(uid)
                    .update("lastLogin", new Date())
                    .get();

                return userResponse;
            } catch (HttpClientErrorException e) {
                JsonNode errorBody = objectMapper.readTree(e.getResponseBodyAsString());
                String errorMessage = errorBody.path("error").path("message").asText();
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, getFirebaseRestErrorMessage(errorMessage));
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error in signIn method: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to sign in at this time. Please try again.");
        }
    }
    
    public UserRecord getUserProfile(String uid) throws FirebaseAuthException {
        try {
            return FirebaseAuth.getInstance().getUser(uid);
        } catch (FirebaseAuthException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found.");
        }
    }
    
    public void updateUserProfile(String uid, String displayName) throws FirebaseAuthException {
        try {
            // Check if new username is already taken
            QuerySnapshot usernameQuery = FirestoreClient.getFirestore()
                .collection("users")
                .whereEqualTo("username", displayName)
                .get()
                .get();

            if (!usernameQuery.isEmpty() && 
                !usernameQuery.getDocuments().get(0).getId().equals(uid)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This username is already taken. Please choose a different one.");
            }

            // Update in Firebase Auth
            UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(uid)
                .setDisplayName(displayName);
            FirebaseAuth.getInstance().updateUser(request);

            // Update in Firestore
            FirestoreClient.getFirestore()
                .collection("users")
                .document(uid)
                .update("username", displayName)
                .get();
        } catch (FirebaseAuthException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to update profile. Please try again.");
        } catch (InterruptedException | ExecutionException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to update profile at this time. Please try again later.");
        }
    }

    public Map<String, Object> getUserData(String uid) throws FirebaseAuthException {
        try {
            DocumentSnapshot doc = FirestoreClient.getFirestore()
                .collection("users")
                .document(uid)
                .get()
                .get();

            if (!doc.exists()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found.");
            }

            return doc.getData();
        } catch (InterruptedException | ExecutionException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to retrieve user data at this time. Please try again later.");
        }
    }

    public FirebaseAuth getUserAuth() {
        return FirebaseAuth.getInstance();
    }
    
    public Map<String, Object> handleGoogleSignIn(String idToken) {
        try {
            // Exchange Google OAuth token for a Firebase Custom Token
            String signInUrl = String.format("https://identitytoolkit.googleapis.com/v1/accounts:signInWithIdp?key=%s", firebaseApiKey);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("postBody", "id_token=" + idToken + "&providerId=google.com");
            requestBody.put("requestUri", "http://localhost");
            requestBody.put("returnIdpCredential", true);
            requestBody.put("returnSecureToken", true);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                signInUrl,
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                Map.class
            );

            if (response.getBody() == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to authenticate with Google");
            }

            // Extract user information from the response
            Map<String, Object> responseData = response.getBody();
            String uid = (String) responseData.get("localId");
            String email = (String) responseData.get("email");
            String name = (String) responseData.get("displayName");
            String picture = (String) responseData.get("photoUrl");
            
            // Use email prefix if no display name
            if (name == null || name.trim().isEmpty()) {
                name = email.split("@")[0];
            }

            // Check if user exists in Firestore
            DocumentSnapshot userDoc = FirestoreClient.getFirestore()
                .collection("users")
                .document(uid)
                .get()
                .get();

            Map<String, Object> userData;
            if (!userDoc.exists()) {
                // Create new user data
                userData = new HashMap<>();
                userData.put("uid", uid);
                userData.put("email", email);
                userData.put("emailVerified", true);
                userData.put("displayName", name);
                userData.put("photoURL", picture);
                userData.put("accountCreationDate", new Date());
                userData.put("lastLogin", new Date());
                userData.put("provider", "google");
                userData.put("role", "USER");

                // Initialize notification preferences
                Map<String, Boolean> notificationPrefs = new HashMap<>();
                notificationPrefs.put("ownIssues", true);
                notificationPrefs.put("communityActivity", false);
                userData.put("notificationPreferences", notificationPrefs);

                // Generate unique username
                String baseUsername = name.toLowerCase().replaceAll("[^a-z0-9]", "");
                String username = baseUsername;
                int counter = 1;
                
                while (true) {
                    QuerySnapshot usernameQuery = FirestoreClient.getFirestore()
                        .collection("users")
                        .whereEqualTo("username", username)
                        .get()
                        .get();
                    
                    if (usernameQuery.isEmpty()) {
                        break;
                    }
                    username = baseUsername + counter++;
                }
                
                userData.put("username", username);

                // Store in Firestore
                FirestoreClient.getFirestore()
                    .collection("users")
                    .document(uid)
                    .set(userData)
                    .get();
            } else {
                // Update existing user
                userData = userDoc.getData();
                Map<String, Object> updates = new HashMap<>();
                updates.put("lastLogin", new Date());
                
                // Update photo URL if changed
                if (picture != null && !picture.equals(userData.get("photoURL"))) {
                    updates.put("photoURL", picture);
                    userData.put("photoURL", picture);
                }
                
                // Update display name if changed
                if (name != null && !name.equals(userData.get("displayName"))) {
                    updates.put("displayName", name);
                    userData.put("displayName", name);
                }
                
                if (!updates.isEmpty()) {
                    FirestoreClient.getFirestore()
                        .collection("users")
                        .document(uid)
                        .update(updates)
                        .get();
                }
            }

            // Add the Firebase ID token and UID to the response
            userData.put("token", responseData.get("idToken"));
            userData.put("refreshToken", responseData.get("refreshToken"));
            userData.put("uid", uid);
            
            return userData;
        } catch (HttpClientErrorException e) {
            System.err.println("Google Auth Error: " + e.getResponseBodyAsString());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                "Failed to authenticate with Google. Please try again.");
        } catch (Exception e) {
            System.err.println("Error during Google sign-in: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "An error occurred during Google sign-in. Please try again.");
        }
    }
}