package com.ttl.userportal.service;

import com.ttl.userportal.dto.LeaveRequestDTO;
import com.ttl.userportal.dto.LeaveTypeDTO;
import com.ttl.userportal.entity.Employee;
import com.ttl.userportal.entity.EmployeeLeaveBalance;
import com.ttl.userportal.entity.LeaveType;
import com.ttl.userportal.repository.EmployeeLeaveBalanceRepository;
import com.ttl.userportal.repository.EmployeeRepository;
import com.ttl.userportal.repository.LeaveTypeRepository;
import com.ttl.userportal.util.model.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LeaveTypeService {
    
    @Autowired
    private LeaveTypeRepository leaveTypeRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private EmployeeLeaveBalanceRepository employeeLeaveBalanceRepository;
    
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
        mapDtoToEntity(dto, leaveType);
        leaveType.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
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
        
        mapDtoToEntity(dto, leaveType);
        leaveType.setUpdatedAt(LocalDateTime.now());
        
        LeaveType updated = leaveTypeRepository.save(leaveType);
        return convertToDTO(updated);
    }
    
    private void mapDtoToEntity(LeaveTypeDTO dto, LeaveType entity) {
        entity.setTypeCode(dto.getTypeCode());
        entity.setTypeName(dto.getTypeName());
        entity.setDescription(dto.getDescription());
        entity.setNumberOfDays(dto.getNumberOfDays());
        entity.setIsActive(dto.getIsActive());
        entity.setIsCarryForwardAllowed(dto.getIsCarryForwardAllowed() != null ? dto.getIsCarryForwardAllowed() : false);
        entity.setMaxCarryForwardDays(dto.getMaxCarryForwardDays());
        entity.setRequiresApproval(dto.getRequiresApproval() != null ? dto.getRequiresApproval() : true);
        entity.setAdvanceNoticeDays(dto.getAdvanceNoticeDays());
        entity.setMedicalCertRequiredAfterDays(dto.getMedicalCertRequiredAfterDays());
        entity.setApplicableGender(dto.getApplicableGender() != null ? dto.getApplicableGender() : "ALL");
        entity.setMinAge(dto.getMinAge());
        entity.setMaxAge(dto.getMaxAge());
        entity.setExpiryDays(dto.getExpiryDays());
        entity.setIsPaid(dto.getIsPaid() != null ? dto.getIsPaid() : true);
        entity.setAllowHalfDay(dto.getAllowHalfDay() != null ? dto.getAllowHalfDay() : true);
        entity.setMaxPerMonth(dto.getMaxPerMonth());
        entity.setMinHoursForComp(dto.getMinHoursForComp());
    }
    
    public void deleteLeaveType(Integer id) {
        LeaveType leaveType = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave type not found with ID: " + id));
        leaveType.setIsActive(false);
        leaveType.setUpdatedAt(LocalDateTime.now());
        leaveTypeRepository.save(leaveType);
    }
    
    private LeaveTypeDTO convertToDTO(LeaveType leaveType) {
        LeaveTypeDTO dto = new LeaveTypeDTO();
        dto.setLeaveTypeId(leaveType.getLeaveTypeId());
        dto.setTypeCode(leaveType.getTypeCode());
        dto.setTypeName(leaveType.getTypeName());
        dto.setDescription(leaveType.getDescription());
        dto.setNumberOfDays(leaveType.getNumberOfDays());
        dto.setIsActive(leaveType.getIsActive());
        dto.setIsCarryForwardAllowed(leaveType.getIsCarryForwardAllowed());
        dto.setMaxCarryForwardDays(leaveType.getMaxCarryForwardDays());
        dto.setRequiresApproval(leaveType.getRequiresApproval());
        dto.setAdvanceNoticeDays(leaveType.getAdvanceNoticeDays());
        dto.setMedicalCertRequiredAfterDays(leaveType.getMedicalCertRequiredAfterDays());
        dto.setApplicableGender(leaveType.getApplicableGender());
        dto.setMinAge(leaveType.getMinAge());
        dto.setMaxAge(leaveType.getMaxAge());
        dto.setExpiryDays(leaveType.getExpiryDays());
        dto.setIsPaid(leaveType.getIsPaid());
        dto.setAllowHalfDay(leaveType.getAllowHalfDay());
        dto.setMaxPerMonth(leaveType.getMaxPerMonth());
        dto.setMinHoursForComp(leaveType.getMinHoursForComp());
        return dto;
    }

    public Map<String,Object>getLeaveBalanceDetailsForUser(UserDetails userDetails) {
        Employee employee = employeeRepository.findByUserId(userDetails.getUser_id());
        List<EmployeeLeaveBalance> employeeLeaveBalances = employeeLeaveBalanceRepository.findByEmployeeId(employee.getEmployeeId().longValue());

        Map<String, Object> leaveBalanceDetails = new HashMap<>();


        for (EmployeeLeaveBalance elb : employeeLeaveBalances) {

            LeaveType leaveType =
                    leaveTypeRepository.findById(elb.getLeaveTypeId())
                            .orElse(null);

            if (leaveType != null) {
                leaveBalanceDetails.put(
                        leaveType.getTypeName(),
                        elb.getBalanceDays()
                );
            }
        }
        return  leaveBalanceDetails;
    }
    public void updateEmployeeLeaveBalance(LeaveRequestDTO leaveRequestDTO, UserDetails userDetails)
    {
        Integer leaveTypeId;
        
        // Prefer leaveTypeId from DTO, fallback to lookup by type name
        if (leaveRequestDTO.getLeaveTypeId() != null) {
            leaveTypeId = leaveRequestDTO.getLeaveTypeId();
        } else if (leaveRequestDTO.getType() != null) {
            leaveTypeId = leaveTypeRepository
                    .findByTypeName(leaveRequestDTO.getType())
                    .orElseThrow(() -> new RuntimeException("Leave type not found: " + leaveRequestDTO.getType()))
                    .getLeaveTypeId();
        } else {
            throw new IllegalArgumentException("Leave type ID or name is required");
        }

        Employee employee = employeeRepository.findById(leaveRequestDTO.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        Integer employeeId = employee.getEmployeeId();
        
        EmployeeLeaveBalance existingBalance = employeeLeaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeId(employeeId, leaveTypeId)
                .orElseThrow(() -> new RuntimeException("Leave balance not found for employee"));
        
        // Update the existing balance record instead of creating new one
        int numberOfDays = leaveRequestDTO.getNumberOfDays() != null ? leaveRequestDTO.getNumberOfDays() : 0;
        existingBalance.setUsedDays(existingBalance.getUsedDays() + numberOfDays);
        existingBalance.setBalanceDays(existingBalance.getAllocatedDays() - existingBalance.getUsedDays());

        employeeLeaveBalanceRepository.save(existingBalance);
    }

    //creating a employeeBalance record for a new employee
    public void createBalance(Integer empId) {
        List<LeaveType> leaveTypes = leaveTypeRepository.findAll();

        for (LeaveType leaveType : leaveTypes) {

            Integer leaveTypeId = leaveType.getLeaveTypeId();

            Optional<EmployeeLeaveBalance> optionalBalance =
                    employeeLeaveBalanceRepository.findByEmployeeIdAndLeaveTypeId(empId, leaveTypeId);

            if (optionalBalance.isEmpty()) {
                EmployeeLeaveBalance balance = new EmployeeLeaveBalance();
                balance.setEmployeeId(empId);
                balance.setLeaveTypeId(leaveTypeId);
                balance.setAllocatedDays(leaveType.getNumberOfDays());
                balance.setUsedDays(0);
                balance.setBalanceDays(leaveType.getNumberOfDays());

                employeeLeaveBalanceRepository.save(balance);
            }
        }


    }

}

