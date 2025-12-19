package com.ttl.userportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveTypeDTO {
    private Integer leaveTypeId;
    private String typeCode;
    private String typeName;
    private String description;
    private Integer numberOfDays;
    private Boolean isActive;
    private Boolean isCarryForwardAllowed;
    private Integer maxCarryForwardDays;
    private Boolean requiresApproval;
    private Integer advanceNoticeDays;
    private Integer medicalCertRequiredAfterDays;
    private String applicableGender;
    private Integer minAge;
    private Integer maxAge;
    private Integer expiryDays;
    private Boolean isPaid;
    private Boolean allowHalfDay;
    private Integer maxPerMonth;
    private Integer minHoursForComp;
}

