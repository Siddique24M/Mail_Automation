package com.personal.assistant.service;

import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EmailParser {

    // Regex Patterns
    // Date: Matches "24th Oct 2024", "24 Oct 2024", "24th October 2024"
    private static final String DATE_REGEX = "(\\d{1,2}(?:st|nd|rd|th)?\\s+(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\\s+\\d{4})";

    // Link: Standard URL pattern
    private static final String LINK_REGEX = "https?://(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)";

    private final Pattern datePattern = Pattern.compile(DATE_REGEX, Pattern.CASE_INSENSITIVE);
    private final Pattern linkPattern = Pattern.compile(LINK_REGEX, Pattern.CASE_INSENSITIVE);

    public Map<String, String> parseEmail(String subject, String bodyHtml) {
        Map<String, String> extractedData = new HashMap<>();

        // Clean HTML to text
        String plainText = Jsoup.parse(bodyHtml).text();
        String content = subject + " " + plainText; // Search in both

        // Extract Date
        Matcher dateMatcher = datePattern.matcher(content);
        if (dateMatcher.find()) {
            extractedData.put("date", dateMatcher.group(1));
        }

        // Extract Link associated with triggers like "Test Link" or just first link
        // Current logic: Find first link. Improvement: Contextual link finding.
        Matcher linkMatcher = linkPattern.matcher(plainText); // Links in body
        if (linkMatcher.find()) {
            extractedData.put("link", linkMatcher.group(0));
        }

        // Determine Event Type based on keywords
        String lowerContent = content.toLowerCase();
        if (lowerContent.contains("interview")) {
            extractedData.put("type", "Interview");
        } else if (lowerContent.contains("exam") || lowerContent.contains("test")) {
            extractedData.put("type", "Exam");
        } else if (lowerContent.contains("registration") || lowerContent.contains("apply")) {
            extractedData.put("type", "Registration");
        } else {
            extractedData.put("type", "Other");
        }

        return extractedData;
    }

    // Helper to parse date string to LocalDateTime
    public LocalDateTime parseDateString(String dateStr) {
        try {
            // Remove st, nd, rd, th
            String cleanDate = dateStr.replaceAll("(?<=\\d)(st|nd|rd|th)", "");
            // Pattern to match "24 Oct 2024" or "24 October 2024"
            DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                    .appendPattern("[d MMM yyyy][d MMMM yyyy]")
                    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                    .toFormatter(Locale.ENGLISH);

            return LocalDateTime.parse(cleanDate, formatter);
            // Note: LocalDateTime requires time, defaulted to 00:00
        } catch (Exception e) {
            System.err.println("Could not parse date: " + dateStr);
            return null;
        }
    }
}
