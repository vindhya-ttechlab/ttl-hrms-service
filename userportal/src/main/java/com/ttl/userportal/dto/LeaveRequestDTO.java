package com.ttl.userportal.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LeaveRequestDTO
{
    private Long id;
    private String fromDate;
    private String toDate;
    private String reason;
    private String type;
    private Integer userId;  // Keep as Integer for database compatibility
    private String userName; // Add userName field for user identification
    private Integer approver;
    private LocalDateTime approvedDate;
    private LocalDateTime appliedDate;
    private Boolean isActive;
    private String leaveStatus;
    private Integer numberOfDays;
    private String comment;
}
