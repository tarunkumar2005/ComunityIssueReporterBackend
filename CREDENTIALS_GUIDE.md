# Credentials Management Guide

This guide explains how to properly manage credentials in the FixIt application for different environments.

## Local Development

For local development, follow these steps:

1. Create a `.env` file in the project root with the following variables:
   ```
   # Firebase Configuration
   FIREBASE_PROJECT_ID=your-firebase-project-id
   FIREBASE_STORAGE_BUCKET=your-firebase-storage-bucket
   FIREBASE_API_KEY=your-firebase-api-key
   
   # JWT Configuration
   JWT_EXPIRATION=86400000
   JWT_REFRESH_EXPIRATION=604800000
   ```

2. Place your Firebase service account JSON file at `src/main/resources/firebase-service-account.json`

3. Ensure both files are in your `.gitignore` to prevent accidental commits

## Production Deployment

For production environments:

1. **Never use the `.env` file in production**

2. Set environment variables directly in your hosting platform:
   - For Docker: Use Docker environment variables
   - For Kubernetes: Use Kubernetes Secrets
   - For AWS: Use Parameter Store or Secrets Manager
   - For Heroku: Set Config Vars
   - For other platforms: Follow their secure environment variable guidelines

3. For the Firebase service account:
   - Store it as a secure secret in your hosting platform
   - Mount it to the application at runtime
   - Consider using a secrets management service

## Environment Variable Reference

| Variable | Description | Example |
|----------|-------------|---------|
| FIREBASE_PROJECT_ID | Your Firebase project ID | fixit-a7775 |
| FIREBASE_STORAGE_BUCKET | Your Firebase storage bucket | fixit-a7775.firebasestorage.app |
| FIREBASE_API_KEY | Your Firebase API key | AIzaSyAxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx |
| JWT_EXPIRATION | JWT token expiration (ms) | 86400000 |
| JWT_REFRESH_EXPIRATION | JWT refresh token expiration (ms) | 604800000 |

## Security Best Practices

1. **Rotate credentials regularly**:
   - Update API keys and service accounts on a regular schedule
   - Immediate rotation if any credential is compromised

2. **Limit permissions**:
   - Use the principle of least privilege for all accounts
   - Create separate service accounts for different environments

3. **Audit access**:
   - Monitor who has access to credentials
   - Review logs for unusual access patterns

4. **Never hardcode credentials**:
   - Even in private repositories
   - Even in "temporary" or "test" code

5. **Use secure transport**:
   - Always use HTTPS for API calls
   - Encrypt sensitive data in transit

## Troubleshooting

If you encounter issues with credentials:

1. Check application logs for environment variable warnings or errors
2. Verify that environment variables are correctly set
3. Confirm service account JSON has correct permissions
4. Restart the application after changing environment variables

## Resources

- [Spring Boot externalized configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [Firebase Admin SDK setup guide](https://firebase.google.com/docs/admin/setup)
- [OWASP secure configuration practices](https://cheatsheetseries.owasp.org/cheatsheets/Securing_Credentials_Cheat_Sheet.html) 