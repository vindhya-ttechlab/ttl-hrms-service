package com.ttl.userportal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for storing account activation tokens
 * Used for one-time login URL for new users to set their password
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "account_activation_tokens")
public class AccountActivationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false;

    @Column(name = "is_expired", nullable = false)
    private Boolean isExpired = false;

    /**
     * Check if the token is still valid (not used and not expired)
     */
    public boolean isValid() {
        return !isUsed && !isExpired && LocalDateTime.now().isBefore(expiresAt);
    }
}

