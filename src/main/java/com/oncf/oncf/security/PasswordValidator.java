package com.oncf.oncf.security;

/**
 * Utility class for validating password strength.
 * Policy:
 * - Minimum 8 characters
 * - At least 1 uppercase letter
 * - At least 1 lowercase letter
 * - At least 1 digit
 * - At least 1 special character
 */
public class PasswordValidator {
    public static boolean isValid(String password) {
        if (password == null) return false;
        // Minimum 8 chars, at least 1 uppercase, 1 lowercase, 1 digit, 1 special char
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        return password.matches(pattern);
    }

    /**
     * Throws IllegalArgumentException with a message if password is invalid.
     */
    public static void validateOrThrow(String password) {
        if (!isValid(password)) {
            throw new IllegalArgumentException(
                "Password must be at least 8 characters long and include at least one uppercase letter, " +
                "one lowercase letter, one digit, and one special character (@$!%*?&)."
            );
        }
    }
} 