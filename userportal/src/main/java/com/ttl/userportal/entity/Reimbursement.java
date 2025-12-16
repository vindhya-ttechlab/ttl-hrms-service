package com.ttl.userportal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing a reimbursement claim by an employee
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reimbursements")
public class Reimbursement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reimbursement_id")
    private Long reimbursementId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", length = 10)
    private String currency = "INR";

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate; // Date when expense occurred

    @Column(name = "merchant_name", length = 200)
    private String merchantName; // Shop/Vendor name

    @Column(name = "payment_method", length = 50)
    private String paymentMethod; // Cash, Card, UPI, etc.

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "approver_id")
    private Integer approverId; // Manager who approves

    @Column(name = "approver_comment", length = 500)
    private String approverComment;

    @Column(name = "approved_amount", precision = 10, scale = 2)
    private BigDecimal approvedAmount; // Can be different from claimed amount

    @Column(name = "applied_date", nullable = false)
    private LocalDateTime appliedDate;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @Column(name = "paid_date")
    private LocalDateTime paidDate; // When amount was disbursed

    @Column(name = "payment_reference", length = 100)
    private String paymentReference; // Transaction ID for payment

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "hr_approver_id")
    private Integer hrApproverId;

    @Column(name = "hr_comment", length = 500)
    private String hrComment;

    @Column(name = "hr_approved_date")
    private LocalDateTime hrApprovedDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private ReimbursementCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id", insertable = false, updatable = false)
    private Users approver;

    @OneToMany(mappedBy = "reimbursement", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReimbursementDocument> documents;

    /**
     * Enum for reimbursement status
     */
    public enum ReimbursementStatus {
        DRAFT,          // Saved but not submitted
        PENDING_MANAGER, // Submitted, awaiting approval

        PENDING_HR,      // Approved by manager, awaiting HR processing
        APPROVED,       // Approved by manager
        REJECTED,       // Rejected by manager
        PROCESSING,     // Being processed for payment
        PAID,           // Amount disbursed
        CANCELLED       // Cancelled by employee

    }
}

