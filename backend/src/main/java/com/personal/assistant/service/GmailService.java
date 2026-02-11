package com.personal.assistant.service;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.personal.assistant.entity.JobEvent;
import com.personal.assistant.entity.UserCredential;
import com.personal.assistant.repository.JobEventRepository;
import com.personal.assistant.repository.UserCredentialRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class GmailService {

    private static final String APPLICATION_NAME = "Personal Assistant";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections
            .singletonList("https://www.googleapis.com/auth/gmail.readonly");

    @Value("${google.client.client-id}")
    private String clientId;

    @Value("${google.client.client-secret}")
    private String clientSecret;

    @Value("${google.client.redirect-uri}")
    private String redirectUri;

    private final UserCredentialRepository userCredentialRepository;
    private final JobEventRepository jobEventRepository;
    private final EmailParser emailParser;
    private NetHttpTransport httpTransport;

    public GmailService(UserCredentialRepository userCredentialRepository, JobEventRepository jobEventRepository,
            EmailParser emailParser) {
        this.userCredentialRepository = userCredentialRepository;
        this.jobEventRepository = jobEventRepository;
        this.emailParser = emailParser;
        try {
            this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getAuthorizationUrl() throws IOException {
        GoogleAuthorizationCodeFlow flow = getFlow();
        return flow.newAuthorizationUrl().setRedirectUri(redirectUri).setAccessType("offline").build();
    }

    public void exchangeCode(String code) throws IOException {
        GoogleAuthorizationCodeFlow flow = getFlow();
        TokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirectUri).execute();

        // Save credentials
        UserCredential userCredential = new UserCredential();
        userCredential.setUserId("me"); // Assuming single user for now
        userCredential.setAccessToken(response.getAccessToken());
        userCredential.setRefreshToken(response.getRefreshToken());
        userCredential
                .setExpirationTimeMilliseconds(System.currentTimeMillis() + response.getExpiresInSeconds() * 1000);

        userCredentialRepository.save(userCredential);
    }

    private GoogleAuthorizationCodeFlow getFlow() throws IOException {
        GoogleClientSecrets.Details web = new GoogleClientSecrets.Details();
        web.setClientId(clientId);
        web.setClientSecret(clientSecret);
        GoogleClientSecrets secrets = new GoogleClientSecrets().setWeb(web);

        return new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, secrets, SCOPES)
                .setDataStoreFactory(new MemoryDataStoreFactory()) // We manage persistence manually
                .build();
    }

    private Gmail getGmailClient() throws IOException {
        Optional<UserCredential> credentialOpt = userCredentialRepository.findById("me");
        if (credentialOpt.isEmpty()) {
            throw new IOException("User not authenticated");
        }
        UserCredential stored = credentialOpt.get();

        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(JSON_FACTORY)
                .setClientSecrets(clientId, clientSecret)
                .build();

        credential.setAccessToken(stored.getAccessToken());
        credential.setRefreshToken(stored.getRefreshToken());

        // Refresh if needed (simplified check)
        if (stored.getExpirationTimeMilliseconds() != null
                && stored.getExpirationTimeMilliseconds() < System.currentTimeMillis()) {
            if (credential.refreshToken()) {
                stored.setAccessToken(credential.getAccessToken());
                stored.setExpirationTimeMilliseconds(System.currentTimeMillis() + 3600 * 1000); // approx
                userCredentialRepository.save(stored);
            }
        }

        return new Gmail.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    @Transactional
    public void fetchAndSaveNewEvents() {
        try {
            // 1. Clean up old events first
            LocalDateTime tenDaysAgo = LocalDateTime.now().minusDays(10);
            jobEventRepository.deleteByCreatedAtBefore(tenDaysAgo);

            // 2. Remove legacy events (missing messageId) to prevent duplicates during
            // transition
            jobEventRepository.deleteByMessageIdIsNull();

            Gmail service = getGmailClient();

            // Search for keywords, newer than 10 days
            String query = "subject:interview OR subject:exam OR subject:test OR subject:registration OR subject:screening newer_than:10d";
            ListMessagesResponse response = service.users().messages().list("me").setQ(query).execute();

            List<Message> messages = response.getMessages();
            if (messages == null || messages.isEmpty()) {
                System.out.println("No messages found.");
                return;
            }

            for (Message msg : messages) {
                // Check if already processed
                if (jobEventRepository.existsByMessageId(msg.getId())) {
                    System.out.println("Skipping duplicate message: " + msg.getId());
                    continue;
                }

                Message fullMsg = service.users().messages().get("me", msg.getId()).execute();

                String subject = "";
                String senderEmail = "";
                String senderName = "";

                // Extract headers
                for (var header : fullMsg.getPayload().getHeaders()) {
                    if (header.getName().equalsIgnoreCase("Subject")) {
                        subject = header.getValue();
                    }
                    if (header.getName().equalsIgnoreCase("From")) {
                        senderEmail = extractEmailAddress(header.getValue());
                        senderName = extractSenderName(header.getValue());
                    }
                }

                String body = getBody(fullMsg);
                System.out.println("Processing email: " + subject);
                Map<String, String> data = emailParser.parseEmail(subject, body);
                System.out.println("Parsed data: " + data);

                // Always save if it matched the subject query
                JobEvent event = new JobEvent();
                event.setCompanyName(senderName);
                event.setSubject(subject);
                event.setEventType(data.getOrDefault("type", "Other"));
                event.setActionLink(data.get("link"));
                event.setMessageId(msg.getId());
                event.setSenderEmail(senderEmail);

                if (data.containsKey("date")) {
                    event.setEventDate(emailParser.parseDateString(data.get("date")));
                }

                // Fallback: If no date found in text, use email received date
                if (event.getEventDate() == null) {
                    long internalDateVal = fullMsg.getInternalDate();
                    LocalDateTime emailDate = Instant.ofEpochMilli(internalDateVal)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();
                    event.setEventDate(emailDate);
                    System.out.println("Using email date as fallback: " + emailDate);
                }

                event.setReminded(false);

                jobEventRepository.save(event);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getBody(Message message) {
        // Recursive get body from parts
        // Simplified version
        if (message.getPayload().getBody().getData() != null) {
            return new String(message.getPayload().getBody().decodeData());
        }
        if (message.getPayload().getParts() != null) {
            for (MessagePart part : message.getPayload().getParts()) {
                if (part.getMimeType().equals("text/html") && part.getBody().getData() != null) {
                    return new String(part.getBody().decodeData());
                }
            }
        }
        return "";
    }

    // Extract email address from "From" header
    // Example: "John Doe <john@example.com>" -> "john@example.com"
    private String extractEmailAddress(String fromHeader) {
        if (fromHeader == null || fromHeader.isEmpty()) {
            return "";
        }

        // Check if email is in angle brackets
        int start = fromHeader.indexOf('<');
        int end = fromHeader.indexOf('>');

        if (start != -1 && end != -1 && end > start) {
            return fromHeader.substring(start + 1, end).trim();
        }

        // If no angle brackets, assume the whole string is the email
        // or extract using regex
        String emailRegex = "([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(emailRegex);
        java.util.regex.Matcher matcher = pattern.matcher(fromHeader);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return fromHeader.trim();
    }

    private String extractSenderName(String fromHeader) {
        if (fromHeader == null || fromHeader.isEmpty()) {
            return "Unknown";
        }
        // "Google <no-reply@accounts.google.com>" -> "Google"
        int start = fromHeader.indexOf('<');
        if (start > 0) {
            return fromHeader.substring(0, start).trim().replace("\"", "");
        }
        // If just email, or no brackets
        if (fromHeader.contains("@")) {
            return extractEmailAddress(fromHeader); // Fallback to email
        }
        return fromHeader.replace("\"", "").trim();
    }

    // Get user profile information (name and email)
    public Map<String, String> getUserInfo() {
        Map<String, String> userInfo = new HashMap<>();
        try {
            Gmail service = getGmailClient();
            com.google.api.services.gmail.model.Profile profile = service.users().getProfile("me").execute();

            userInfo.put("email", profile.getEmailAddress());

            // Extract first name from email (before @)
            String email = profile.getEmailAddress();
            if (email != null && email.contains("@")) {
                String localPart = email.substring(0, email.indexOf("@"));
                // Capitalize first letter
                String firstName = localPart.substring(0, 1).toUpperCase() + localPart.substring(1);
                userInfo.put("name", firstName);
            } else {
                userInfo.put("name", "User");
            }

        } catch (Exception e) {
            System.err.println("Error getting user info: " + e.getMessage());
            userInfo.put("name", "User");
            userInfo.put("email", "");
        }
        return userInfo;
    }

    public void clearUserData() {
        userCredentialRepository.deleteAll();
        jobEventRepository.deleteAll();
    }
}
