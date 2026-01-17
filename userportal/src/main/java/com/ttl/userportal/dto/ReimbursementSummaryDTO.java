package com.ttl.userportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ReimbursementSummaryDTO {
    private long pendingCount;
    private long approvedCount;
    private long rejectedCount;
    private BigDecimal totalApprovedAmount;
    private BigDecimal pendingAmount;
    private int year;
}
