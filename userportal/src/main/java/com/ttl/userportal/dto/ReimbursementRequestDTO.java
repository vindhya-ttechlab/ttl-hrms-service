package com.ttl.userportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for creating/updating reimbursement requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReimbursementRequestDTO {
    private Long reimbursementId; // Null for new, populated for update
    private Integer categoryId;
    private String title;
    private String description;
    private BigDecimal amount;
    private String currency;
    private LocalDate expenseDate;
    private String merchantName;
    private String paymentMethod;
    private Boolean submitForApproval; // true = submit, false = save as draft
    private Integer managerId; // optional override for approver (frontend read-only shows assigned manager)

}

