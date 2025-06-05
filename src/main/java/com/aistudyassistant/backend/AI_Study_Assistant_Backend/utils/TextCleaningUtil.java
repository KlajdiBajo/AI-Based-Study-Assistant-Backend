package com.aistudyassistant.backend.AI_Study_Assistant_Backend.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;

@Component
public class TextCleaningUtil {

    private static final Logger logger = LoggerFactory.getLogger(TextCleaningUtil.class);

    // Pattern to match control characters except common whitespace
    private static final Pattern CONTROL_CHARS_PATTERN = Pattern.compile("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F-\\x9F]");

    // Pattern to match problematic Unicode characters
    private static final Pattern PROBLEMATIC_UNICODE_PATTERN = Pattern.compile("[\\uFFFE\\uFFFF\\u0000-\\u0008\\u000B\\u000C\\u000E-\\u001F\\u007F-\\u009F]");

    /**
     * Comprehensive text cleaning to remove null bytes and other problematic characters
     * that can cause PostgreSQL UTF-8 encoding errors
     */
    public String cleanText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // Log if problematic characters are detected
        if (containsNullBytes(text) || containsControlChars(text)) {
            logger.warn("⚠️ SPRING BOOT: Problematic characters detected in text: {}",
                    text.length() > 100 ? text.substring(0, 100) + "..." : text);
        }

        String cleaned = text;

        // Step 1: Character-by-character cleaning (most aggressive)
        StringBuilder cleanedChars = new StringBuilder();
        for (char c : cleaned.toCharArray()) {
            int charCode = (int) c;
            // Keep only printable characters and safe whitespace
            if (charCode >= 32 || c == '\n' || c == '\t' || c == '\r') {
                // Additional check for invisible/zero-width characters
                if (c != '\u200B' && c != '\u200C' && c != '\u200D' && c != '\uFEFF' && c != '\u00AD') {
                    cleanedChars.append(c);
                }
            } else {
                logger.warn("⚠️ SPRING BOOT: Removed character with code {}: '{}'", charCode, Character.toString(c));
            }
        }
        cleaned = cleanedChars.toString();

        // Step 2: Remove all variations of null bytes
        cleaned = cleaned.replace("\u0000", "");  // Unicode null
        cleaned = cleaned.replace("\0", "");      // String null

        // Step 3: Remove control characters except common whitespace (\n, \t, \r, space)
        cleaned = CONTROL_CHARS_PATTERN.matcher(cleaned).replaceAll("");

        // Step 4: Remove problematic Unicode characters
        cleaned = PROBLEMATIC_UNICODE_PATTERN.matcher(cleaned).replaceAll("");

        // Step 5: Normalize whitespace
        cleaned = cleaned.replaceAll("\\s+", " ").trim();

        // Step 6: Validate encoding by encoding/decoding
        try {
            byte[] bytes = cleaned.getBytes("UTF-8");
            cleaned = new String(bytes, "UTF-8");
        } catch (Exception e) {
            logger.error("❌ SPRING BOOT: UTF-8 encoding error during cleaning", e);
            // Fallback: remove non-ASCII characters
            cleaned = cleaned.replaceAll("[^\\x00-\\x7F]", "");
        }

        // Step 7: Final null byte check
        if (containsNullBytes(cleaned)) {
            logger.error("⚠️ SPRING BOOT: NULL BYTES STILL PRESENT AFTER CLEANING!");
            // Nuclear option: character by character filtering
            StringBuilder sb = new StringBuilder();
            for (char c : cleaned.toCharArray()) {
                if (c >= 32 || c == '\n' || c == '\t' || c == '\r') {
                    sb.append(c);
                }
            }
            cleaned = sb.toString();
        }

        // Step 8: Length limit for safety
        if (cleaned.length() > 10000) {
            cleaned = cleaned.substring(0, 10000) + "...";
            logger.warn("⚠️ SPRING BOOT: Text truncated to 10000 characters");
        }

        return cleaned;
    }

    /**
     * Clean a Map of options (for MCQ options)
     */
    public Map<String, String> cleanOptions(Map<String, String> options) {
        if (options == null) {
            return new HashMap<>();
        }

        Map<String, String> cleanedOptions = new HashMap<>();
        for (Map.Entry<String, String> entry : options.entrySet()) {
            String cleanKey = cleanText(entry.getKey());
            String cleanValue = cleanText(entry.getValue());

            if (!cleanKey.isEmpty() && !cleanValue.isEmpty()) {
                cleanedOptions.put(cleanKey, cleanValue);
            }
        }

        return cleanedOptions;
    }

    /**
     * Check if text contains null bytes
     */
    public boolean containsNullBytes(String text) {
        if (text == null) return false;
        return text.contains("\u0000") || text.contains("\0");
    }

    /**
     * Check if text contains control characters
     */
    public boolean containsControlChars(String text) {
        if (text == null) return false;
        return CONTROL_CHARS_PATTERN.matcher(text).find();
    }

    /**
     * Validate that text is safe for database insertion
     */
    public boolean isTextSafeForDatabase(String text) {
        if (text == null) return true;

        return !containsNullBytes(text) &&
                !containsControlChars(text) &&
                text.length() <= 10000;
    }

    /**
     * Clean and validate text, throwing exception if still problematic
     */
    public String cleanAndValidate(String text, String fieldName) {
        String cleaned = cleanText(text);

        if (!isTextSafeForDatabase(cleaned)) {
            throw new IllegalArgumentException(
                    String.format("SPRING BOOT: Text field '%s' contains problematic characters even after cleaning: %s",
                            fieldName, cleaned.length() > 50 ? cleaned.substring(0, 50) + "..." : cleaned)
            );
        }

        return cleaned;
    }
}
