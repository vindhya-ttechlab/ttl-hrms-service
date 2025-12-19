package com.ttl.userportal.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LeaveRequestDTO
{
    private Long id;
    private String fromDate;
    private String toDate;
    private String reason;
    
    // Leave type - frontend should send leaveTypeId
    private Integer leaveTypeId;
    private String leaveTypeCode; // For reference (EL, SL, ML, etc.)
    private String type; // Leave type name (for backward compatibility and display)
    
    private Integer employeeId;
    private String employeeName;
    private Integer approver;
    private String approverName;
    private LocalDateTime approvedDate;
    private LocalDateTime appliedDate;
    private Boolean isActive;
    private String leaveStatus;
    private Integer numberOfDays;
    private String comment;
    
    // Additional fields for validation info from LeaveType
    private Boolean requiresMedicalCertificate;
    private String documentUrl; // For uploading medical certificate
}
