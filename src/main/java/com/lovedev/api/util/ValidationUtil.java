package com.lovedev.api.util;

import com.lovedev.api.exception.BadRequestException;
import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

/**
 * Utility class for validation operations
 */
@UtilityClass
public class ValidationUtil {

    // Regex patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[1-9]\\d{1,14}$"
    );

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_-]{3,20}$"
    );

    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$"
    );

    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate email and throw exception if invalid
     */
    public static void validateEmail(String email) {
        if (!isValidEmail(email)) {
            throw new BadRequestException("Invalid email format: " + email);
        }
    }

    /**
     * Validate phone number format (international format)
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validate phone and throw exception if invalid
     */
    public static void validatePhone(String phone) {
        if (!isValidPhone(phone)) {
            throw new BadRequestException("Invalid phone number format: " + phone);
        }
    }

    /**
     * Validate password strength
     * At least 8 characters, one uppercase, one lowercase, one digit, one special character
     */
    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Validate password and throw exception if invalid
     */
    public static void validatePassword(String password) {
        if (!isValidPassword(password)) {
            throw new BadRequestException(
                    "Password must be at least 8 characters long and contain at least " +
                            "one uppercase letter, one lowercase letter, one digit, and one special character"
            );
        }
    }

    /**
     * Validate username format
     */
    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * Validate username and throw exception if invalid
     */
    public static void validateUsername(String username) {
        if (!isValidUsername(username)) {
            throw new BadRequestException(
                    "Username must be 3-20 characters long and contain only letters, numbers, underscores, and hyphens"
            );
        }
    }

    /**
     * Validate URL format
     */
    public static boolean isValidUrl(String url) {
        return url != null && URL_PATTERN.matcher(url).matches();
    }

    /**
     * Validate URL and throw exception if invalid
     */
    public static void validateUrl(String url) {
        if (!isValidUrl(url)) {
            throw new BadRequestException("Invalid URL format: " + url);
        }
    }

    /**
     * Validate string is not null or empty
     */
    public static boolean isNotEmpty(String value) {
        return StringUtils.hasText(value);
    }

    /**
     * Validate string and throw exception if empty
     */
    public static void validateNotEmpty(String value, String fieldName) {
        if (!isNotEmpty(value)) {
            throw new BadRequestException(fieldName + " cannot be empty");
        }
    }

    /**
     * Validate string length
     */
    public static boolean isValidLength(String value, int min, int max) {
        if (value == null) return false;
        int length = value.length();
        return length >= min && length <= max;
    }

    /**
     * Validate string length and throw exception if invalid
     */
    public static void validateLength(String value, int min, int max, String fieldName) {
        if (!isValidLength(value, min, max)) {
            throw new BadRequestException(
                    String.format("%s must be between %d and %d characters", fieldName, min, max)
            );
        }
    }

    /**
     * Validate date of birth (must be in past and person must be at least minAge years old)
     */
    public static boolean isValidDateOfBirth(LocalDate dateOfBirth, int minAge) {
        if (dateOfBirth == null || dateOfBirth.isAfter(LocalDate.now())) {
            return false;
        }
        Period period = Period.between(dateOfBirth, LocalDate.now());
        return period.getYears() >= minAge;
    }

    /**
     * Validate date of birth and throw exception if invalid
     */
    public static void validateDateOfBirth(LocalDate dateOfBirth, int minAge) {
        if (!isValidDateOfBirth(dateOfBirth, minAge)) {
            throw new BadRequestException(
                    String.format("Date of birth must be in the past and person must be at least %d years old", minAge)
            );
        }
    }

    /**
     * Validate numeric range
     */
    public static boolean isInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }

    /**
     * Validate numeric range and throw exception if invalid
     */
    public static void validateRange(int value, int min, int max, String fieldName) {
        if (!isInRange(value, min, max)) {
            throw new BadRequestException(
                    String.format("%s must be between %d and %d", fieldName, min, max)
            );
        }
    }

    /**
     * Validate numeric range (long)
     */
    public static boolean isInRange(long value, long min, long max) {
        return value >= min && value <= max;
    }

    /**
     * Validate numeric range (long) and throw exception if invalid
     */
    public static void validateRange(long value, long min, long max, String fieldName) {
        if (!isInRange(value, min, max)) {
            throw new BadRequestException(
                    String.format("%s must be between %d and %d", fieldName, min, max)
            );
        }
    }

    /**
     * Validate positive number
     */
    public static boolean isPositive(int value) {
        return value > 0;
    }

    /**
     * Validate positive number and throw exception if invalid
     */
    public static void validatePositive(int value, String fieldName) {
        if (!isPositive(value)) {
            throw new BadRequestException(fieldName + " must be positive");
        }
    }

    /**
     * Validate non-negative number
     */
    public static boolean isNonNegative(int value) {
        return value >= 0;
    }

    /**
     * Validate non-negative number and throw exception if invalid
     */
    public static void validateNonNegative(int value, String fieldName) {
        if (!isNonNegative(value)) {
            throw new BadRequestException(fieldName + " must be non-negative");
        }
    }

    /**
     * Validate object is not null
     */
    public static void validateNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new BadRequestException(fieldName + " cannot be null");
        }
    }

    /**
     * Validate UUID format
     */
    public static boolean isValidUUID(String uuid) {
        if (uuid == null) return false;
        try {
            java.util.UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Validate UUID and throw exception if invalid
     */
    public static void validateUUID(String uuid, String fieldName) {
        if (!isValidUUID(uuid)) {
            throw new BadRequestException("Invalid UUID format for " + fieldName);
        }
    }

    /**
     * Validate that string contains only alphanumeric characters
     */
    public static boolean isAlphanumeric(String value) {
        return value != null && value.matches("^[a-zA-Z0-9]+$");
    }

    /**
     * Validate alphanumeric and throw exception if invalid
     */
    public static void validateAlphanumeric(String value, String fieldName) {
        if (!isAlphanumeric(value)) {
            throw new BadRequestException(fieldName + " must contain only alphanumeric characters");
        }
    }

    /**
     * Validate that string contains only letters
     */
    public static boolean isAlphabetic(String value) {
        return value != null && value.matches("^[a-zA-Z]+$");
    }

    /**
     * Validate alphabetic and throw exception if invalid
     */
    public static void validateAlphabetic(String value, String fieldName) {
        if (!isAlphabetic(value)) {
            throw new BadRequestException(fieldName + " must contain only letters");
        }
    }

    /**
     * Validate that string contains only numeric characters
     */
    public static boolean isNumeric(String value) {
        return value != null && value.matches("^[0-9]+$");
    }

    /**
     * Validate numeric and throw exception if invalid
     */
    public static void validateNumeric(String value, String fieldName) {
        if (!isNumeric(value)) {
            throw new BadRequestException(fieldName + " must contain only numbers");
        }
    }

    /**
     * Sanitize string input (remove potential XSS characters)
     */
    public static String sanitizeInput(String input) {
        if (input == null) return null;

        return input
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&#x27;")
                .replaceAll("/", "&#x2F;");
    }

    /**
     * Validate pagination parameters
     */
    public static void validatePaginationParams(int page, int size, int maxSize) {
        if (page < 0) {
            throw new BadRequestException("Page number cannot be negative");
        }
        if (size <= 0) {
            throw new BadRequestException("Page size must be positive");
        }
        if (size > maxSize) {
            throw new BadRequestException(
                    String.format("Page size cannot exceed maximum of %d", maxSize)
            );
        }
    }

    /**
     * Validate sort direction
     */
    public static void validateSortDirection(String direction) {
        if (direction != null &&
                !direction.equalsIgnoreCase("asc") &&
                !direction.equalsIgnoreCase("desc")) {
            throw new BadRequestException("Sort direction must be 'asc' or 'desc'");
        }
    }

    /**
     * Validate file extension
     */
    public static boolean hasValidExtension(String fileName, String... allowedExtensions) {
        if (fileName == null || fileName.isEmpty()) return false;

        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        for (String allowed : allowedExtensions) {
            if (extension.equals(allowed.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validate file extension and throw exception if invalid
     */
    public static void validateFileExtension(String fileName, String... allowedExtensions) {
        if (!hasValidExtension(fileName, allowedExtensions)) {
            throw new BadRequestException(
                    "Invalid file extension. Allowed: " + String.join(", ", allowedExtensions)
            );
        }
    }
}