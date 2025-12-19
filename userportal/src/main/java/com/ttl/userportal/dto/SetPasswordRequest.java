package com.ttl.userportal.dto;

import lombok.Data;

/**
 * Request DTO for setting password via activation token
 */
@Data
public class SetPasswordRequest {
    private String token;
    private String email;
    private String password;
    private String confirmPassword;
}

