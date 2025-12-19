package com.ttl.userportal.dto;

import lombok.Data;

/**
 * Request DTO for validating activation token with email
 */
@Data
public class ActivationTokenValidationRequest {
    private String token;
    private String email;
}

