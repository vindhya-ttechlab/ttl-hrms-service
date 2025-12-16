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
import org.springframework.transaction.annotation.Transactional;

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
        Integer leaveTypeId = leaveTypeRepository
                .findByTypeName(leaveRequestDTO.getType())
                .get()
                .getLeaveTypeId();

        Long employeeId=employeeRepository.findByUserId(userDetails.getUser_id()).getUserId();
        EmployeeLeaveBalance getEmployeeBalance=employeeLeaveBalanceRepository.findByEmployeeIdAndLeaveTypeId(employeeId,leaveTypeId).get();
        EmployeeLeaveBalance employeeLeaveBalance=new EmployeeLeaveBalance();

        employeeLeaveBalance.setEmployeeId(employeeId);
        employeeLeaveBalance.setLeaveTypeId(leaveTypeId);
        employeeLeaveBalance.setBalanceDays(getEmployeeBalance.getBalanceDays()-leaveRequestDTO.getNumberOfDays());

        employeeLeaveBalanceRepository.save(employeeLeaveBalance);


    }

    //creating a employeeBalance record for a new employee
    public void getOrCreateBalance(Integer employeeId) {
        Long empId = employeeId.longValue();
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

