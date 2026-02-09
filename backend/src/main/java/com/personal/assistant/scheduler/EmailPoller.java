package com.personal.assistant.scheduler;

import com.personal.assistant.service.GmailService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EmailPoller {

    private final GmailService gmailService;

    public EmailPoller(GmailService gmailService) {
        this.gmailService = gmailService;
    }

    // Runs every 4 hours (14400000 ms)
    @Scheduled(fixedRate = 14400000)
    public void scanInbox() {
        System.out.println("Polling Gmail for new events...");
        try {
            gmailService.fetchAndSaveNewEvents();
        } catch (Exception e) {
            System.err.println("Failed to poll Gmail: " + e.getMessage());
            // Log full stack trace if needed, but for now simple error message suffices
            e.printStackTrace();
        }
    }
}
