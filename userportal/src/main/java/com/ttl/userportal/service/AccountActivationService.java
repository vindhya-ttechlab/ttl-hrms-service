package com.ttl.userportal.service;

import com.ttl.userportal.dto.ActivationTokenResponse;
import com.ttl.userportal.dto.SetPasswordRequest;
import com.ttl.userportal.entity.AccountActivationToken;
import com.ttl.userportal.entity.Users;
import com.ttl.userportal.repository.AccountActivationTokenRepository;
import com.ttl.userportal.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

/**
 * Service for managing account activation tokens
 * Handles token generation, validation, and password setting
 */
@Slf4j
@Service
public class AccountActivationService {

    @Autowired
    private AccountActivationTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailNotificationService emailNotificationService;

    @Value("${app.activation.token.expiry-days:2}")
    private int tokenExpiryDays;

    @Value("${app.activation.base-url:http://localhost:5173/activate}")
    private String activationBaseUrl;

    @Value("${app.email.template.new-user-welcome:NEW_USER_WELCOME}")
    private String newUserWelcomeTemplate;

    /**
     * Generate a new activation token for a user
     * @param user The user to generate token for
     * @return The activation URL
     */
    @Transactional
    public String generateActivationToken(Users user) {
        // Invalidate any existing tokens for this user
        tokenRepository.invalidateTokensForUser(Long.valueOf(user.getId()));

        // Generate secure random token
        String token = generateSecureToken();

        // Create new activation token
        AccountActivationToken activationToken = new AccountActivationToken();
        activationToken.setToken(token);
        activationToken.setUserId(Long.valueOf(user.getId()));
        activationToken.setEmail(user.getEmail());
        activationToken.setCreatedAt(LocalDateTime.now());
        activationToken.setExpiresAt(LocalDateTime.now().plusDays(tokenExpiryDays));
        activationToken.setIsUsed(false);
        activationToken.setIsExpired(false);

        tokenRepository.save(activationToken);

        log.info("Generated activation token for user: {}", user.getEmail());

        return buildActivationUrl(token);
    }

    /**
     * Validate if a token exists and is valid
     * @param token The token to validate
     * @return ActivationTokenResponse
     */
    public ActivationTokenResponse validateToken(String token) {
        Optional<AccountActivationToken> tokenOpt = tokenRepository
                .findByTokenAndIsUsedFalseAndIsExpiredFalse(token);

        if (tokenOpt.isEmpty()) {
            return new ActivationTokenResponse(false, "Invalid or expired activation link", null, null);
        }

        AccountActivationToken activationToken = tokenOpt.get();

        // Check if token has expired by time
        if (LocalDateTime.now().isAfter(activationToken.getExpiresAt())) {
            activationToken.setIsExpired(true);
            tokenRepository.save(activationToken);
            return new ActivationTokenResponse(false, "Activation link has expired", null, null);
        }

        // Get user details
        Optional<Users> userOpt = userRepository.findById(activationToken.getUserId().intValue());
        if (userOpt.isEmpty()) {
            return new ActivationTokenResponse(false, "User not found", null, null);
        }

        Users user = userOpt.get();
        String maskedEmail = maskEmail(user.getEmail());

        return new ActivationTokenResponse(true, "Token is valid", maskedEmail, user.getName());
    }

    /**
     * Validate token with email verification
     * @param token The token
     * @param email The email to verify
     * @return ActivationTokenResponse
     */
    public ActivationTokenResponse validateTokenWithEmail(String token, String email) {
        ActivationTokenResponse response = validateToken(token);
        
        if (!response.isValid()) {
            return response;
        }

        Optional<AccountActivationToken> tokenOpt = tokenRepository
                .findByTokenAndIsUsedFalseAndIsExpiredFalse(token);

        if (tokenOpt.isEmpty()) {
            return new ActivationTokenResponse(false, "Invalid activation link", null, null);
        }

        AccountActivationToken activationToken = tokenOpt.get();

        // Verify email matches
        if (!activationToken.getEmail().equalsIgnoreCase(email.trim())) {
            return new ActivationTokenResponse(false, "Email does not match. Please enter the email address where you received the activation link.", null, null);
        }

        Optional<Users> userOpt = userRepository.findById(activationToken.getUserId().intValue());
        if (userOpt.isEmpty()) {
            return new ActivationTokenResponse(false, "User not found", null, null);
        }

        Users user = userOpt.get();
        return new ActivationTokenResponse(true, "Email verified successfully", user.getEmail(), user.getName());
    }

    /**
     * Set password using activation token
     * @param request SetPasswordRequest containing token, email, and password
     * @return ActivationTokenResponse
     */
    @Transactional
    public ActivationTokenResponse setPassword(SetPasswordRequest request) {
        // Validate password match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return new ActivationTokenResponse(false, "Passwords do not match", null, null);
        }

        // Validate password strength
        if (request.getPassword().length() < 8) {
            return new ActivationTokenResponse(false, "Password must be at least 8 characters long", null, null);
        }

        // Validate token and email
        ActivationTokenResponse validation = validateTokenWithEmail(request.getToken(), request.getEmail());
        if (!validation.isValid()) {
            return validation;
        }

        Optional<AccountActivationToken> tokenOpt = tokenRepository
                .findByTokenAndIsUsedFalseAndIsExpiredFalse(request.getToken());

        if (tokenOpt.isEmpty()) {
            return new ActivationTokenResponse(false, "Invalid activation link", null, null);
        }

        AccountActivationToken activationToken = tokenOpt.get();

        // Get and update user
        Optional<Users> userOpt = userRepository.findById(activationToken.getUserId().intValue());
        if (userOpt.isEmpty()) {
            return new ActivationTokenResponse(false, "User not found", null, null);
        }

        Users user = userOpt.get();

        // Update password
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        user.setIsFirstLogin(false); // No longer first login after setting password
        userRepository.save(user);

        // Mark token as used
        activationToken.setIsUsed(true);
        activationToken.setUsedAt(LocalDateTime.now());
        tokenRepository.save(activationToken);

        log.info("Password set successfully for user: {}", user.getEmail());

        return new ActivationTokenResponse(true, "Password set successfully. You can now login.", user.getEmail(), user.getName());
    }

    /**
     * Resend activation email
     * @param email The user's email
     * @return ActivationTokenResponse
     */
    @Transactional
    public ActivationTokenResponse resendActivation(String email) {
        Users user = userRepository.findByEmail(email.trim());
        
        if (user == null) {
            // Don't reveal if user exists or not for security
            return new ActivationTokenResponse(true, "If the email exists in our system, a new activation link will be sent.", null, null);
        }

        // Generate new token
        String activationUrl = generateActivationToken(user);

        // Send email
        sendActivationEmail(user, activationUrl);

        return new ActivationTokenResponse(true, "A new activation link has been sent to your email.", null, null);
    }

    /**
     * Send activation email to user
     */
    public void sendActivationEmail(Users user, String activationUrl) {
        java.util.Map<String, Object> emailData = new java.util.HashMap<>();
        emailData.put("employee_name", user.getName());
        emailData.put("name", user.getName());
        emailData.put("email", user.getEmail());
        emailData.put("activation_url", activationUrl);
        emailData.put("expiry_days", String.valueOf(tokenExpiryDays));

        emailNotificationService.sendTemplatedEmail(newUserWelcomeTemplate, user.getEmail(), emailData);
    }

    /**
     * Generate a secure random token
     */
    private String generateSecureToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    /**
     * Build the activation URL
     */
    private String buildActivationUrl(String token) {
        return activationBaseUrl + "?token=" + token;
    }

    /**
     * Mask email for display (e.g., j***@example.com)
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***@***.***";
        }
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];

        String maskedLocal;
        if (localPart.length() <= 2) {
            maskedLocal = localPart.charAt(0) + "***";
        } else {
            maskedLocal = localPart.charAt(0) + "***" + localPart.charAt(localPart.length() - 1);
        }

        return maskedLocal + "@" + domain;
    }

    /**
     * Scheduled task to mark expired tokens
     * Runs every hour
     */
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupExpiredTokens() {
        int count = tokenRepository.markExpiredTokens(LocalDateTime.now());
        if (count > 0) {
            log.info("Marked {} expired activation tokens", count);
        }
    }
}

