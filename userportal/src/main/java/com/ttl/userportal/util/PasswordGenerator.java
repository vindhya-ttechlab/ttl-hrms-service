package com.ttl.userportal.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for generating secure random passwords
 */
public class PasswordGenerator {
    
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARS = "@#$%&*!";
    
    private static final SecureRandom random = new SecureRandom();
    
    /**
     * Generates a secure random password with at least one uppercase, lowercase, digit, and special character
     * @param length The length of the password (minimum 8)
     * @return Generated password
     */
    public static String generatePassword(int length) {
        if (length < 8) {
            length = 8; // Minimum password length
        }
        
        List<Character> passwordChars = new ArrayList<>();
        
        // Ensure at least one character from each category
        passwordChars.add(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        passwordChars.add(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        passwordChars.add(DIGITS.charAt(random.nextInt(DIGITS.length())));
        passwordChars.add(SPECIAL_CHARS.charAt(random.nextInt(SPECIAL_CHARS.length())));
        
        // Fill the rest with random characters from all categories
        String allChars = UPPERCASE + LOWERCASE + DIGITS + SPECIAL_CHARS;
        for (int i = 4; i < length; i++) {
            passwordChars.add(allChars.charAt(random.nextInt(allChars.length())));
        }
        
        // Shuffle to randomize position
        Collections.shuffle(passwordChars, random);
        
        // Build the password string
        StringBuilder password = new StringBuilder();
        for (Character c : passwordChars) {
            password.append(c);
        }
        
        return password.toString();
    }
    
    /**
     * Generates a secure random password with default length of 12 characters
     * @return Generated password
     */
    public static String generatePassword() {
        return generatePassword(12);
    }
}

