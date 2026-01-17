package com.ttl.userportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReimbursementDTO {
    private Long reimbursementId;
    private Integer userId;
    private String userName;
    private String userEmail;
    private Integer categoryId;
    private String categoryName;
    private String title;
    private String description;
    private BigDecimal amount;
    private String currency;
    private LocalDate expenseDate;
    private String merchantName;
    private String paymentMethod;
    private String status;
    private Integer approverId;
    private String approverName;
    private String approverComment;
    private BigDecimal approvedAmount;
    private LocalDateTime appliedDate;
    private LocalDateTime approvedDate;
    private LocalDateTime paidDate;
    private String paymentReference;
    private String rejectionReason;

    // HR approval info
    private Integer hrApproverId;
    private String hrApproverName;
    private LocalDateTime hrApprovedDate;
    private String hrComment;

    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Documents attached to this reimbursement
    private List<ReimbursementDocumentDTO> documents;
}

