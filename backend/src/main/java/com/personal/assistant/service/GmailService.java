package com.personal.assistant.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.personal.assistant.entity.JobEvent;
import com.personal.assistant.entity.UserCredential;
import com.personal.assistant.repository.JobEventRepository;
import com.personal.assistant.repository.UserCredentialRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Collections;
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

    public void fetchAndSaveNewEvents() {
        try {
            Gmail service = getGmailClient();

            // Search for keywords
            String query = "subject:interview OR subject:exam OR subject:test OR subject:registration";
            ListMessagesResponse response = service.users().messages().list("me").setQ(query).execute();

            List<Message> messages = response.getMessages();
            if (messages == null || messages.isEmpty()) {
                System.out.println("No messages found.");
                return;
            }

            for (Message msg : messages) {
                // Check if already processed (could use ID in DB to avoid dupes)
                // For now, parsing every time is inefficient, improvement: store processed
                // Message IDs.

                Message fullMsg = service.users().messages().get("me", msg.getId()).execute();

                String subject = "";
                // Extract headers
                for (var header : fullMsg.getPayload().getHeaders()) {
                    if (header.getName().equalsIgnoreCase("Subject")) {
                        subject = header.getValue();
                    }
                }

                String body = getBody(fullMsg);
                Map<String, String> data = emailParser.parseEmail(subject, body);

                if (data.containsKey("date")) {
                    // It's a valid event
                    JobEvent event = new JobEvent();
                    event.setCompanyName(subject); // Simplified
                    event.setEventType(data.get("type"));
                    event.setActionLink(data.get("link"));
                    event.setEventDate(emailParser.parseDateString(data.get("date")));
                    event.setReminded(false);

                    // Simple dupe check by date & company
                    // Real app should check message ID
                    jobEventRepository.save(event);
                }
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
}
