package com.ttl.userportal.service;

import com.ttl.userportal.dto.LeaveTypeDTO;
import com.ttl.userportal.entity.LeaveType;
import com.ttl.userportal.repository.LeaveTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveTypeService {
    
    @Autowired
    private LeaveTypeRepository leaveTypeRepository;
    
    public List<LeaveTypeDTO> getAllLeaveTypes() {
        return leaveTypeRepository.findByIsActiveTrueOrderByTypeName().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<LeaveTypeDTO> getAllLeaveTypesIncludingInactive() {
        return leaveTypeRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public LeaveTypeDTO getLeaveTypeById(Integer id) {
        LeaveType leaveType = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave type not found with ID: " + id));
        return convertToDTO(leaveType);
    }
    
    public LeaveTypeDTO createLeaveType(LeaveTypeDTO dto) {
        if (leaveTypeRepository.existsByTypeName(dto.getTypeName())) {
            throw new RuntimeException("Leave type with name '" + dto.getTypeName() + "' already exists");
        }
        
        LeaveType leaveType = new LeaveType();
        leaveType.setTypeName(dto.getTypeName());
        leaveType.setDescription(dto.getDescription());
        leaveType.setNumberOfDays(dto.getNumberOfDays());
        leaveType.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        leaveType.setIsCarryForwardAllowed(dto.getIsCarryForwardAllowed() != null ? dto.getIsCarryForwardAllowed() : false);
        leaveType.setMaxCarryForwardDays(dto.getMaxCarryForwardDays());
        leaveType.setRequiresApproval(dto.getRequiresApproval() != null ? dto.getRequiresApproval() : true);
        leaveType.setCreatedAt(LocalDateTime.now());
        leaveType.setUpdatedAt(LocalDateTime.now());
        
        LeaveType saved = leaveTypeRepository.save(leaveType);
        return convertToDTO(saved);
    }
    
    public LeaveTypeDTO updateLeaveType(Integer id, LeaveTypeDTO dto) {
        LeaveType leaveType = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave type not found with ID: " + id));
        
        // Check if type name is being changed and if new name already exists
        if (!leaveType.getTypeName().equals(dto.getTypeName()) && 
            leaveTypeRepository.existsByTypeName(dto.getTypeName())) {
            throw new RuntimeException("Leave type with name '" + dto.getTypeName() + "' already exists");
        }
        
        leaveType.setTypeName(dto.getTypeName());
        leaveType.setDescription(dto.getDescription());
        leaveType.setNumberOfDays(dto.getNumberOfDays());
        leaveType.setIsActive(dto.getIsActive());
        leaveType.setIsCarryForwardAllowed(dto.getIsCarryForwardAllowed());
        leaveType.setMaxCarryForwardDays(dto.getMaxCarryForwardDays());
        leaveType.setRequiresApproval(dto.getRequiresApproval());
        leaveType.setUpdatedAt(LocalDateTime.now());
        
        LeaveType updated = leaveTypeRepository.save(leaveType);
        return convertToDTO(updated);
    }
    
    public void deleteLeaveType(Integer id) {
        LeaveType leaveType = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave type not found with ID: " + id));
        
        // Soft delete - set isActive to false
        leaveType.setIsActive(false);
        leaveType.setUpdatedAt(LocalDateTime.now());
        leaveTypeRepository.save(leaveType);
    }
    
    private LeaveTypeDTO convertToDTO(LeaveType leaveType) {
        return new LeaveTypeDTO(
                leaveType.getLeaveTypeId(),
                leaveType.getTypeName(),
                leaveType.getDescription(),
                leaveType.getNumberOfDays(),
                leaveType.getIsActive(),
                leaveType.getIsCarryForwardAllowed(),
                leaveType.getMaxCarryForwardDays(),
                leaveType.getRequiresApproval()
        );
    }
}

