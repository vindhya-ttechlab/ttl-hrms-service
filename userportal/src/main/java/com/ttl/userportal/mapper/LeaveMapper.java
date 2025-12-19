package com.ttl.userportal.mapper;

import com.ttl.userportal.dto.LeaveRequestDTO;
import com.ttl.userportal.entity.Employee;
import com.ttl.userportal.entity.LeaveEntity;
import com.ttl.userportal.entity.LeaveType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class LeaveMapper {
    public LeaveEntity mapToLeaveEntity(LeaveRequestDTO dto, Integer employeeId, 
                                       Integer approverId, LocalDateTime appliedDate,
                                       LeaveType leaveType) {
        LeaveEntity leaveEntity = new LeaveEntity();
        leaveEntity.setFromDate(LocalDate.parse(dto.getFromDate()));
        leaveEntity.setToDate(LocalDate.parse(dto.getToDate()));
        leaveEntity.setReason(dto.getReason());
        leaveEntity.setLeaveTypeId(leaveType.getLeaveTypeId());
        leaveEntity.setType(leaveType.getTypeName());
        leaveEntity.setEmployeeId(employeeId);
        leaveEntity.setApprover(approverId);
        leaveEntity.setAppliedDate(appliedDate);
        leaveEntity.setApprovedDate(dto.getApprovedDate());
        leaveEntity.setIsActive(true);
        leaveEntity.setLeaveStatus(dto.getLeaveStatus() != null ? dto.getLeaveStatus() : "Pending");
        leaveEntity.setNumberOfDays(dto.getNumberOfDays());
        leaveEntity.setDocumentUrl(dto.getDocumentUrl());
        return leaveEntity;
    }

    /**
     * Backward compatible method - uses type name from DTO
     */
    public LeaveEntity mapToLeaveEntity(LeaveRequestDTO dto, Integer employeeId, 
                                       Integer approverId, LocalDateTime appliedDate) {
        LeaveEntity leaveEntity = new LeaveEntity();
        leaveEntity.setFromDate(LocalDate.parse(dto.getFromDate()));
        leaveEntity.setToDate(LocalDate.parse(dto.getToDate()));
        leaveEntity.setReason(dto.getReason());
        leaveEntity.setLeaveTypeId(dto.getLeaveTypeId());
        leaveEntity.setType(dto.getType());
        leaveEntity.setEmployeeId(employeeId);
        leaveEntity.setApprover(approverId);
        leaveEntity.setAppliedDate(appliedDate);
        leaveEntity.setApprovedDate(dto.getApprovedDate());
        leaveEntity.setIsActive(true);
        leaveEntity.setLeaveStatus(dto.getLeaveStatus() != null ? dto.getLeaveStatus() : "Pending");
        leaveEntity.setNumberOfDays(dto.getNumberOfDays());
        leaveEntity.setDocumentUrl(dto.getDocumentUrl());
        return leaveEntity;
    }

    /**
     * Updates existing LeaveEntity with data from LeaveRequestDTO
     */
    public void updateLeaveEntity(LeaveEntity leaveEntity, LeaveRequestDTO dto, 
                                 Integer employeeId, Integer approverId) {
        leaveEntity.setFromDate(LocalDate.parse(dto.getFromDate()));
        leaveEntity.setToDate(LocalDate.parse(dto.getToDate()));
        leaveEntity.setReason(dto.getReason());
        if (dto.getLeaveTypeId() != null) {
            leaveEntity.setLeaveTypeId(dto.getLeaveTypeId());
        }
        if (dto.getType() != null) {
            leaveEntity.setType(dto.getType());
        }
        leaveEntity.setEmployeeId(employeeId);
        leaveEntity.setApprover(approverId);
        leaveEntity.setAppliedDate(dto.getAppliedDate());
        leaveEntity.setApprovedDate(dto.getApprovedDate());
        leaveEntity.setIsActive(true);
        if (dto.getLeaveStatus() != null) {
            leaveEntity.setLeaveStatus(dto.getLeaveStatus());
        }
        leaveEntity.setNumberOfDays(dto.getNumberOfDays());
        leaveEntity.setDocumentUrl(dto.getDocumentUrl());
    }

    /**
     * Maps LeaveEntity to LeaveRequestDTO
     */
    public LeaveRequestDTO mapToLeaveRequestDTO(LeaveEntity leaveEntity) {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setId(leaveEntity.getId());
        dto.setFromDate(String.valueOf(leaveEntity.getFromDate()));
        dto.setToDate(String.valueOf(leaveEntity.getToDate()));
        dto.setReason(leaveEntity.getReason());
        dto.setLeaveTypeId(leaveEntity.getLeaveTypeId());
        dto.setType(leaveEntity.getType());
        dto.setEmployeeId(leaveEntity.getEmployeeId());
        dto.setApprover(leaveEntity.getApprover());
        dto.setAppliedDate(leaveEntity.getAppliedDate());
        dto.setApprovedDate(leaveEntity.getApprovedDate());
        dto.setIsActive(leaveEntity.getIsActive());
        dto.setLeaveStatus(leaveEntity.getLeaveStatus());
        dto.setNumberOfDays(leaveEntity.getNumberOfDays());
        dto.setComment(leaveEntity.getManagerComment());
        dto.setDocumentUrl(leaveEntity.getDocumentUrl());
        return dto;
    }

    /**
     * Maps LeaveEntity to LeaveRequestDTO with Employee name
     * @param leaveEntity LeaveEntity
     * @param employee Employee entity
     * @return LeaveRequestDTO
     */
    public LeaveRequestDTO mapToLeaveRequestDTO(LeaveEntity leaveEntity, Employee employee) {
        LeaveRequestDTO dto = mapToLeaveRequestDTO(leaveEntity);
        if (employee != null) {
            dto.setEmployeeId(employee.getEmployeeId());
            dto.setEmployeeName(employee.getName());
        }
        return dto;
    }

    /**
     * Updates LeaveEntity for approval/rejection
     * @param leaveEntity LeaveEntity to update
     * @param status Approval status ("Approved" or "Rejected")
     * @param approvedDate Approval date
     * @param comment Manager comment
     */
    public void updateLeaveStatus(LeaveEntity leaveEntity, String status, 
                                 LocalDateTime approvedDate, String comment) {
        leaveEntity.setLeaveStatus(status);
        leaveEntity.setApprovedDate(approvedDate);
        leaveEntity.setManagerComment(comment);
    }
}

