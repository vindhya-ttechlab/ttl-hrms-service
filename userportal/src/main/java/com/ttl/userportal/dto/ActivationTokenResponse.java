package com.ttl.userportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for activation token operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivationTokenResponse {
    private boolean valid;
    private String message;
    private String email; // Masked email for display
    private String userName;
}

