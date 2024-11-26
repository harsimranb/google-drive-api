# Getting Started

This is a Spring Rest API Java application that allows users to list, download, upload, and delete files in Google Drive.
This application uses Google OAuth 2.0 Authorization flow to authenticate the user.

# Run Instructions
To run and test this application
1. Setup Google API OAuth Credentials: authorize
2. In `src/main/resources/application.properties`, set `client-id` and `client-secret`.
3. Execute: `./gradlew clean build bootRun`.  The application is live on `localhost:8080`.
4. Test application in Swagger: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html).
   1. You will prompted to login to your Google Account and authorize this application.

# Future Enhancements
- Error Handling for Google OAuth Flows, Google Drive API Exceptions
- End-to-end API Integration Tests using WebClient
- Ability to query, page, and sort files from Google Drive
- Improve Unit tests
  - Current test cases are only covering success cases, we want to also cover failed, error, validation cases.
  - Add more specific parameter checks using `eq()`, rather than `any()`
