package com.ttl.userportal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "REIMBURSEMENT_CATEGORIES")
public class ReimbursementCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CATEGORY_ID")
    private Integer categoryId;

    @Column(name = "CATEGORY_NAME", nullable = false, unique = true, length = 100)
    private String categoryName;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    @Column(name = "MAX_AMOUNT")
    private BigDecimal maxAmount; // Maximum claimable amount per request

    @Column(name = "REQUIRES_RECEIPT", nullable = false)
    private Boolean requiresReceipt = true;

    @Column(name = "REQUIRES_APPROVAL", nullable = false)
    private Boolean requiresApproval = true;

    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean isActive = true;

    @Column(name = "CREATED_AT", updatable = false, nullable =false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        if (this.requiresReceipt == null) {
            this.requiresReceipt = true;
        }
        if (this.requiresApproval == null) {
            this.requiresApproval = true;
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

