package com.ttl.userportal.controller;

import com.ttl.userportal.dto.ActivationTokenResponse;
import com.ttl.userportal.dto.ActivationTokenValidationRequest;
import com.ttl.userportal.dto.SetPasswordRequest;
import com.ttl.userportal.service.AccountActivationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for account activation endpoints
 * These endpoints are public (no authentication required)
 */
@RestController
@RequestMapping("/api/auth/activate")
@CrossOrigin(origins = "*")
public class AccountActivationController {

    @Autowired
    private AccountActivationService activationService;

    /**
     * Validate if an activation token is valid
     * GET /api/auth/activate/validate?token=xxx
     */
    @GetMapping("/validate")
    public ResponseEntity<ActivationTokenResponse> validateToken(@RequestParam String token) {
        ActivationTokenResponse response = activationService.validateToken(token);
        
        if (response.isValid()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Verify email for a given token
     * POST /api/auth/activate/verify-email
     * Body: { "token": "xxx", "email": "user@example.com" }
     */
    @PostMapping("/verify-email")
    public ResponseEntity<ActivationTokenResponse> verifyEmail(@RequestBody ActivationTokenValidationRequest request) {
        ActivationTokenResponse response = activationService.validateTokenWithEmail(
                request.getToken(), 
                request.getEmail()
        );
        
        if (response.isValid()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Set password using activation token
     * POST /api/auth/activate/set-password
     * Body: { "token": "xxx", "email": "user@example.com", "password": "xxx", "confirmPassword": "xxx" }
     */
    @PostMapping("/set-password")
    public ResponseEntity<ActivationTokenResponse> setPassword(@RequestBody SetPasswordRequest request) {
        ActivationTokenResponse response = activationService.setPassword(request);
        
        if (response.isValid()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Resend activation email
     * POST /api/auth/activate/resend
     * Body: { "email": "user@example.com" }
     */
    @PostMapping("/resend")
    public ResponseEntity<ActivationTokenResponse> resendActivation(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new ActivationTokenResponse(false, "Email is required", null, null)
            );
        }
        
        ActivationTokenResponse response = activationService.resendActivation(email);
        return ResponseEntity.ok(response);
    }
}

