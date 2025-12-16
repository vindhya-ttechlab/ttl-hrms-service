package com.ttl.userportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReimbursementCategoryDTO {
    private Integer categoryId;
    private String categoryName;
    private String description;
    private BigDecimal maxAmount;
    private Boolean requiresReceipt;
    private Boolean requiresApproval;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

