package com.ttl.userportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for approving/rejecting reimbursement requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReimbursementApprovalDTO {
    private Long reimbursementId;
    private String action; // APPROVE, REJECT
    private String comment;
    private BigDecimal approvedAmount; // Can be different from requested amount
    private String rejectionReason; // Required if action is REJECT
}

