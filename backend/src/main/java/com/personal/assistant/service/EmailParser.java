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
    // Date: Matches multiple formats:
    // - "24th Oct 2024", "24 Oct 2024", "24th October 2024"
    // - "Oct 24, 2024", "October 24, 2024"
    // - "2024-10-24", "24/10/2024", "10/24/2024", "24-10-2024"
    private static final String DATE_REGEX = "(\\d{1,2}(?:st|nd|rd|th)?\\s+(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\\s+\\d{4})|"
            + // 24 Oct 2024
            "((?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\\s+\\d{1,2}(?:st|nd|rd|th)?,\\s+\\d{4})|" + // Oct
                                                                                                                    // 24,
                                                                                                                    // 2024
            "(\\d{4}-\\d{2}-\\d{2})|" + // 2024-10-24
            "(\\d{1,2}/\\d{1,2}/\\d{4})|" + // 24/10/2024 or 10/24/2024
            "(\\d{1,2}-\\d{1,2}-\\d{4})|" + // 24-10-2024
            "(\\d{1,2}\\.\\d{1,2}\\.\\d{4})"; // 24.10.2024

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
            // Use group(0) to get the entire matched text, not group(1)
            // This is important because our regex has multiple alternatives (|)
            String matchedDate = dateMatcher.group(0);
            extractedData.put("date", matchedDate);
            System.out.println("Found date: " + matchedDate);
        } else {
            System.out.println("No date found in: " + content.substring(0, Math.min(200, content.length())));
        }

        // Extract Link associated with triggers like "Test Link" or just first link
        // Current logic: Find first link. Improvement: Contextual link finding.
        Matcher linkMatcher = linkPattern.matcher(plainText); // Links in body
        if (linkMatcher.find()) {
            extractedData.put("link", linkMatcher.group(0));
        }

        // Determine Event Type based on keywords
        String lowerContent = content.toLowerCase();
        if (lowerContent.contains("screening")) {
            extractedData.put("type", "Other");
        } else if (lowerContent.contains("interview")) {
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

    // Helper to parse date string to LocalDateTime - supports multiple formats
    public LocalDateTime parseDateString(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        try {
            // Remove st, nd, rd, th suffixes
            String cleanDate = dateStr.replaceAll("(?<=\\d)(st|nd|rd|th)", "").trim();

            // Try multiple date formats
            DateTimeFormatter[] formatters = {
                    // "24 Oct 2024" or "24 October 2024"
                    new DateTimeFormatterBuilder()
                            .appendPattern("[d MMM yyyy][d MMMM yyyy]")
                            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                            .toFormatter(Locale.ENGLISH),

                    // "Oct 24, 2024" or "October 24, 2024"
                    new DateTimeFormatterBuilder()
                            .appendPattern("[MMM d, yyyy][MMMM d, yyyy]")
                            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                            .toFormatter(Locale.ENGLISH),

                    // "2024-10-24" (ISO format)
                    new DateTimeFormatterBuilder()
                            .appendPattern("yyyy-MM-dd")
                            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                            .toFormatter(Locale.ENGLISH),

                    // "24/10/2024" or "10/24/2024"
                    new DateTimeFormatterBuilder()
                            .appendPattern("[dd/MM/yyyy][MM/dd/yyyy]")
                            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                            .toFormatter(Locale.ENGLISH),

                    // "24-10-2024" or "10-24-2024"
                    new DateTimeFormatterBuilder()
                            .appendPattern("[dd-MM-yyyy][MM-dd-yyyy]")
                            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                            .toFormatter(Locale.ENGLISH),

                    // "24.10.2024"
                    new DateTimeFormatterBuilder()
                            .appendPattern("dd.MM.yyyy")
                            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                            .toFormatter(Locale.ENGLISH)
            };

            // Try each formatter
            for (DateTimeFormatter formatter : formatters) {
                try {
                    return LocalDateTime.parse(cleanDate, formatter);
                } catch (Exception e) {
                    // Try next formatter
                }
            }

            // If all formatters fail, try parsing as LocalDate and convert
            try {
                LocalDate localDate = LocalDate.parse(cleanDate, DateTimeFormatter.ISO_LOCAL_DATE);
                return localDate.atStartOfDay();
            } catch (Exception e) {
                // Continue to error logging
            }

            System.err.println("Could not parse date with any format: " + dateStr);
            return null;
        } catch (Exception e) {
            System.err.println("Error parsing date: " + dateStr + " - " + e.getMessage());
            return null;
        }
    }
}
