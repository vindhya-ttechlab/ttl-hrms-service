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
    private Long reimbursementId;
    private Integer categoryId;
    private String title;
    private String description;
    private BigDecimal amount;
    private String currency;
    private LocalDate expenseDate;
    private String merchantName;
    private String paymentMethod;
    private Boolean submitForApproval;
    private Integer managerId;
}

