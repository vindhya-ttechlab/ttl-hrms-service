package com.ttl.userportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveTypeDTO {
    private Integer leaveTypeId;
    private String typeName;
    private String description;
    private Integer numberOfDays;
    private Boolean isActive;
    private Boolean isCarryForwardAllowed;
    private Integer maxCarryForwardDays;
    private Boolean requiresApproval;
}

