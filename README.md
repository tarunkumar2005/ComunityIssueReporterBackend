# FixIt - Community Issue Reporting Platform

FixIt is a RESTful API backend for a community-driven platform that allows users to report and track local infrastructure issues. Users can report problems, add images, upvote issues, and track the progress as authorities address them.

## 🌟 Features

### 👤 User Authentication and Management
- ✅ Email/Password-based signup
- ✅ Email/Password-based login
- ✅ Google Authentication support
- ✅ User profile management
- ✅ Profile editing
- ✅ Account deletion

### 🔐 Admin Authentication and Management
- ✅ Email/Password-based signup with phone number and location requirements
- ✅ Email/Password-based login
- ✅ Google Authentication with additional phone number and location collection
- ✅ Admin profile management with performance metrics
- ✅ Dashboard with analytics and issue handling statistics
- ✅ Permission-based access control

### 📝 Issue Management
- ✅ Create new issues with title, description, location, and coordinates
- ✅ Upload images for issues
- ✅ View issue details (title, description, location, coordinates, images, upvotes)
- ✅ Update issue information
- ✅ Delete issues (only by the reporter)
- ✅ Track issue status (OPEN, IN_PROGRESS, RESOLVED, CLOSED, REJECTED)
- ✅ Admin status change tracking with notes and history

### 🔍 Issue Discovery and Filtering
- ✅ Get paginated list of issues
- ✅ Filter issues by status, location, reporter, date range, and minimum upvotes
- ✅ Sort issues by newest, oldest, most upvoted, least upvoted, and recently updated
- ✅ Search issues by keywords (in title, description, location)

### 👍 Upvoting System
- ✅ Upvote issues to show support
- ✅ Check if a user has already upvoted an issue
- ✅ Track total upvotes for each issue

### 👤 User Profile and Stats
- ✅ View user profile information
- ✅ View user stats (reported issues, upvoted issues)
- ✅ Update notification preferences

### 📊 Admin Dashboard and Analytics
- ✅ Track number of issues resolved, closed, and rejected by admin
- ✅ Calculate approval rate (issues resolved vs. total issues handled)
- ✅ View status change history with timestamps and notes
- ✅ System-wide analytics for issue reporting and resolution
- ✅ Location-based admin assignment for issues

## 🛠️ Technical Stack

- Java Spring Boot
- Firebase Authentication
- Firebase Firestore Database
- Firebase Storage (for images)

## 🚀 API Endpoints

### User Authentication
- `POST /api/auth/signup` - Create a new user account
- `POST /api/auth/signin` - Sign in with email and password
- `POST /api/auth/google` - Sign in with Google

### Admin Authentication
- `POST /api/admin/auth/signup` - Create a new admin account (with phone number and location)
- `POST /api/admin/auth/signin` - Sign in as admin
- `POST /api/admin/auth/google` - Sign in as admin with Google (requires phone number and location)
- `POST /api/admin/auth/verify` - Verify if admin token is valid

### User Management
- `GET /api/users/profile/{uid}` - Get user profile
- `PUT /api/users/profile/{uid}` - Update user profile
- `PUT /api/users/profile/{uid}/notifications` - Update notification preferences
- `DELETE /api/users/profile/{uid}` - Delete user account

### Admin Management
- `GET /api/admin/profile/{uid}` - Get admin profile with stats
- `GET /api/admin/dashboard/{uid}` - Get admin dashboard stats
- `GET /api/admin/analytics` - Get system-wide analytics

### Issues
- `POST /api/issues` - Create a new issue
- `GET /api/issues` - Get list of issues with filtering and pagination
- `GET /api/issues/{issueId}` - Get issue details
- `PUT /api/issues/{issueId}` - Update issue
- `DELETE /api/issues/{issueId}` - Delete issue
- `PATCH /api/issues/{issueId}/status` - Update issue status (user)
- `POST /api/issues/{issueId}/upvote` - Upvote an issue
- `GET /api/issues/{issueId}/upvote/check` - Check if user has upvoted an issue

### Admin Issue Management
- `GET /api/admin/issues` - Get issues for admin (with filters)
- `PATCH /api/admin/issues/{issueId}/status` - Update issue status with admin notes
- `GET /api/admin/issues/{issueId}/history` - Get issue status change history

### Images
- `POST /api/images/upload` - Upload images
- `DELETE /api/images/{filename}` - Delete an image

## Credentials Management

### Environment Variables

This application uses environment variables to manage sensitive credentials. Here's how to set them up:

1. Create a `.env` file in the project root (use `.env.example` as a template)
2. Add your actual credentials to this file
3. The application will use these values, or fall back to defaults if not found

**Important Security Notes:**
- NEVER commit `.env` files to version control
- For production deployments, set environment variables directly in your hosting environment
- The Firebase service account JSON file should be kept secure and not committed to version control

### Required Credentials

The following environment variables are required:

- `FIREBASE_PROJECT_ID`: Your Firebase project ID
- `FIREBASE_STORAGE_BUCKET`: Your Firebase storage bucket name
- `FIREBASE_API_KEY`: Your Firebase API key
- `JWT_EXPIRATION`: JWT token expiration time in milliseconds
- `JWT_REFRESH_EXPIRATION`: JWT refresh token expiration time in milliseconds

## 🏁 Getting Started

### Prerequisites
- Java 17+
- Maven
- Firebase account
- Firebase project

### Configuration
1. Create a Firebase project and download the service account key
2. Place the key at `src/main/resources/firebase-service-account.json`
3. Configure Firebase settings in `application.properties`

### Running the Application
```bash
mvn spring-boot:run
```

## 📄 License
This project is licensed under the MIT License 