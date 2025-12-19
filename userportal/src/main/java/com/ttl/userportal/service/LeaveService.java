package com.ttl.userportal.service;

import com.ttl.userportal.dto.LeaveRequestDTO;
import com.ttl.userportal.entity.*;
import com.ttl.userportal.mapper.LeaveMapper;
import com.ttl.userportal.repository.*;
import com.ttl.userportal.util.model.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LeaveService
{
    @Autowired
    LeaveRepository leaveRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    EmailNotificationService emailNotificationService;

    @Autowired
    LeaveMapper leaveMapper;
    
    @Autowired
    LeaveTypeService leaveTypeService;

    @Autowired
    LeaveTypeRepository leaveTypeRepos;

    @Autowired
    EmployeeLeaveBalanceRepository employeeLeaveBalanceRepository;

    @Value("${app.email.template.leave-request:LEAVE_REQUEST}")
    private String leaveRequestTemplate;

    @Value("${app.email.template.leave-approved:LEAVE_APPROVED}")
    private String leaveApprovedTemplate;

    @Value("${app.email.template.leave-rejected:LEAVE_REJECTED}")
    private String leaveRejectedTemplate;

    //save LeaveRequest
    public void saveOrUpdateLeaveRequest(LeaveRequestDTO leaveRequest, UserDetails userDetails)
    {
        LocalDateTime now=LocalDateTime.now();
        // Validate that leave request is not null
        if(leaveRequest == null)
        {
            throw new IllegalArgumentException("Leave request cannot be null");
        }
        
        // Get LeaveType from ID provided by frontend
        LeaveType leaveType = null;
        if (leaveRequest.getLeaveTypeId() != null) {
            leaveType = leaveTypeRepos.findById(leaveRequest.getLeaveTypeId())
                    .orElseThrow(() -> new RuntimeException("Leave type not found with ID: " + leaveRequest.getLeaveTypeId()));
            // Set type name from LeaveType for consistency
            leaveRequest.setType(leaveType.getTypeName());
            leaveRequest.setLeaveTypeCode(leaveType.getTypeCode());
        } else if (leaveRequest.getType() != null) {
            // Backward compatibility - find by type name
            leaveType = leaveTypeRepos.findByTypeName(leaveRequest.getType())
                    .orElseThrow(() -> new RuntimeException("Leave type not found: " + leaveRequest.getType()));
            leaveRequest.setLeaveTypeId(leaveType.getLeaveTypeId());
            leaveRequest.setLeaveTypeCode(leaveType.getTypeCode());
        } else {
            throw new IllegalArgumentException("Leave type ID or type name is required");
        }
        
        Integer userId = Math.toIntExact(userDetails.getUser_id());
        Users users = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User Not found"));
        Employee employee = employeeRepository.findByEmail(users.getEmail())
                .orElseThrow(() -> new RuntimeException("Employee not found for user email: " + users.getEmail()));

        // Validate leave type rules
        validateLeaveRequest(leaveRequest, leaveType, employee);

        Integer managerEmployeeId = employee.getManagerId();
        Employee managerEmployee = null;
        if (managerEmployeeId != null) {
            managerEmployee = employeeRepository.findById(managerEmployeeId)
                    .orElseThrow(() -> new RuntimeException("Manager employee not found"));
        }
        if (managerEmployee != null) {
            LocalDate fromDate = LocalDate.parse(leaveRequest.getFromDate());
            LocalDate endDate = LocalDate.parse(leaveRequest.getToDate());
            String numberOfDays = leaveRequest.getNumberOfDays() != null ? 
                String.valueOf(leaveRequest.getNumberOfDays()) : "Not specified";
            
            // Build source data for template - template code "LEAVE_REQUEST" comes from database
            Map<String, Object> emailData = new HashMap<>();
            emailData.put("employee_name", employee.getName());
            emailData.put("manager_name", managerEmployee.getName());
            emailData.put("leave_type", leaveType.getTypeName());
            emailData.put("from_date", fromDate.toString());
            emailData.put("to_date", endDate.toString());
            emailData.put("number_of_days", numberOfDays);
            emailData.put("reason", leaveRequest.getReason());
            
            emailNotificationService.sendTemplatedEmail(leaveRequestTemplate, managerEmployee.getEmail(), emailData);
        }
        
        if(leaveRequest.getId() == null) {
            // Create new leave using mapper with LeaveType
            LeaveEntity leaveEntity = leaveMapper.mapToLeaveEntity(
                leaveRequest, 
                employee.getEmployeeId(), 
                managerEmployeeId, 
                now,
                leaveType
            );
            leaveRepository.save(leaveEntity);
            
            // Update leave balance - new leave with Pending status
            if (leaveEntity.getLeaveStatus() != null && "Pending".equalsIgnoreCase(leaveEntity.getLeaveStatus())) {
                leaveTypeService.updateEmployeeLeaveBalance(
                    leaveRequest,userDetails
                );
            }
        } else {
            // Update existing leave using mapper
            LeaveEntity leaveEntity = leaveRepository.findByIdAndIsActive(leaveRequest.getId(), true);
            if (leaveEntity == null) {
                throw new IllegalArgumentException("Leave request with ID " + leaveRequest.getId() + " not found or inactive");
            }
            
            String oldStatus = leaveEntity.getLeaveStatus();
            leaveMapper.updateLeaveEntity(leaveEntity, leaveRequest, employee.getEmployeeId(), managerEmployeeId);
            leaveRepository.save(leaveEntity);
            
            // Update leave balance if status changed
            String newStatus = leaveEntity.getLeaveStatus();
            if (oldStatus != null && newStatus != null && !oldStatus.equalsIgnoreCase(newStatus)) {
                leaveTypeService.updateEmployeeLeaveBalance(
                        leaveRequest,userDetails
                );
            }
        }
    }

    //getUpcomingLeaveData
    public List<LeaveRequestDTO> upcomingLeaves(String userName, UserDetails userDetails)
    {
        LocalDate today = LocalDate.now();
        Integer userId= Math.toIntExact(userDetails.getUser_id());
        
        // Get Employee by email
        Users users = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User Not found"));
        Employee employee = employeeRepository.findByEmail(users.getEmail())
                .orElseThrow(() -> new RuntimeException("Employee not found for user email: " + users.getEmail()));

        List<LeaveEntity> upcomingLeaves = leaveRepository.listOfUpcomingLeaves(today, employee.getEmployeeId());

        // Map entities to DTOs using mapper
        return upcomingLeaves.stream()
                .map(leaveMapper::mapToLeaveRequestDTO)
                .collect(Collectors.toList());
    }

    //past leaves
    public List<LeaveRequestDTO> pastLeaves(UserDetails userDetails)
    {

        LocalDate today = LocalDate.now();
        Integer userId= Math.toIntExact(userDetails.getUser_id());
        
        // Get Employee by email
        Users users = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User Not found"));
        Employee employee = employeeRepository.findByEmail(users.getEmail())
                .orElseThrow(() -> new RuntimeException("Employee not found for user email: " + users.getEmail()));

        List<LeaveEntity> pastLeaves = leaveRepository.listOfPastLeaves(today, employee.getEmployeeId());

        // Map entities to DTOs using mapper
        return pastLeaves.stream()
                .map(leaveMapper::mapToLeaveRequestDTO)
                .collect(Collectors.toList());
    }


    public List<LeaveRequestDTO> getLeaves(UserDetails userDetails)
    {
        int currentYear = LocalDate.now().getYear();
        Integer userId= Math.toIntExact(userDetails.getUser_id());
        
        // Get Employee by email to get managerId
        Users users = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User Not found"));
        Employee employee = employeeRepository.findByEmail(users.getEmail())
                .orElseThrow(() -> new RuntimeException("Employee not found for user email: " + users.getEmail()));

        List<LeaveEntity> leaves = leaveRepository.findByApprover(currentYear, employee.getEmployeeId());

        // Map entities to DTOs using mapper
        return leaves.stream()
                .map(leaveEntity -> {
                    Employee leaveEmployee = employeeRepository.findById(leaveEntity.getEmployeeId())
                            .orElseThrow(() -> new RuntimeException("Employee Not found for leave"));
                    return leaveMapper.mapToLeaveRequestDTO(leaveEntity, leaveEmployee);
                })
                .collect(Collectors.toList());
    }


    public void approveLeaveRequest(LeaveRequestDTO leaveRequestDTO,UserDetails userDetails)
    {
        LocalDateTime now=LocalDateTime.now();
        if(leaveRequestDTO.getId()!=null)
        {
            LeaveEntity leaveEntity=leaveRepository.findById(leaveRequestDTO.getId())
                    .orElseThrow(()-> new RuntimeException("Leave Request is Empty"));
            leaveMapper.updateLeaveStatus(leaveEntity, "Approved", now, leaveRequestDTO.getComment());
            leaveRepository.save(leaveEntity);
            leaveTypeService.updateEmployeeLeaveBalance(
                    leaveRequestDTO,userDetails
            );


            Employee employee = employeeRepository.findById(leaveEntity.getEmployeeId())
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            
            Employee managerEmployee = null;
            if (leaveEntity.getApprover() != null) {
                managerEmployee = employeeRepository.findById(leaveEntity.getApprover())
                        .orElseThrow(() -> new RuntimeException("Manager employee not found"));
            }
            
            if (managerEmployee != null) {
                // Build source data for template - template code "LEAVE_APPROVED" comes from database
                Map<String, Object> emailData = new HashMap<>();
                emailData.put("employee_name", employee.getName());
                emailData.put("manager_name", managerEmployee.getName());
                emailData.put("leave_type", leaveEntity.getType());
                emailData.put("from_date", leaveEntity.getFromDate().toString());
                emailData.put("to_date", leaveEntity.getToDate().toString());
                emailData.put("comment", leaveRequestDTO.getComment());
                
                emailNotificationService.sendTemplatedEmail(leaveApprovedTemplate, employee.getEmail(), emailData);
            }
        }
    }

    public void rejectLeaveRequest(LeaveRequestDTO leaveRequestDTO,UserDetails userDetails)
    {
        LocalDateTime now=LocalDateTime.now();
        if(leaveRequestDTO.getId()!=null)
        {
            LeaveEntity leaveEntity=leaveRepository.findById(leaveRequestDTO.getId())
                    .orElseThrow(()-> new RuntimeException("Leave Request is Empty"));
            leaveMapper.updateLeaveStatus(leaveEntity, "Rejected", now, leaveRequestDTO.getComment());
            leaveRepository.save(leaveEntity);
            leaveTypeService.updateEmployeeLeaveBalance(leaveRequestDTO,userDetails);
            Employee employee = employeeRepository.findById(leaveEntity.getEmployeeId())
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            
            Employee managerEmployee = null;
            if (leaveEntity.getApprover() != null) {
                managerEmployee = employeeRepository.findById(leaveEntity.getApprover())
                        .orElseThrow(() -> new RuntimeException("Manager employee not found"));
            }
            
            if (managerEmployee != null) {
                // Build source data for template - template code "LEAVE_REJECTED" comes from database
                Map<String, Object> emailData = new HashMap<>();
                emailData.put("employee_name", employee.getName());
                emailData.put("manager_name", managerEmployee.getName());
                emailData.put("leave_type", leaveEntity.getType());
                emailData.put("from_date", leaveEntity.getFromDate().toString());
                emailData.put("to_date", leaveEntity.getToDate().toString());
                emailData.put("comment", leaveRequestDTO.getComment());
                
                emailNotificationService.sendTemplatedEmail(leaveRejectedTemplate, employee.getEmail(), emailData);
            }
        }
    }
    
    /**
     * Validate leave request based on leave type rules
     * @param leaveRequest LeaveRequestDTO
     * @param leaveType LeaveType entity
     * @param employee Employee entity
     */
    private void validateLeaveRequest(LeaveRequestDTO leaveRequest, LeaveType leaveType, Employee employee) {
        // Validate gender-specific leave (MALE, FEMALE, ALL)
        String applicableGender = leaveType.getApplicableGender();
        if (applicableGender != null && !"ALL".equalsIgnoreCase(applicableGender)) {
            // Note: Gender field needs to be added to Employee entity for full validation
            // For now, skip gender validation if employee gender is not set
        }
        
        // Validate medical certificate requirement for sick leave
        if (leaveType.getMedicalCertRequiredAfterDays() != null && leaveType.getMedicalCertRequiredAfterDays() > 0) {
            if (leaveRequest.getNumberOfDays() != null && 
                leaveRequest.getNumberOfDays() > leaveType.getMedicalCertRequiredAfterDays()) {
                // Flag that medical certificate is required
                if (leaveRequest.getDocumentUrl() == null || leaveRequest.getDocumentUrl().isEmpty()) {
                    leaveRequest.setRequiresMedicalCertificate(true);
                    // Note: We're not blocking here, just flagging - actual enforcement at approval
                }
            }
        }
        
        // Validate advance notice period for planned leave
        if (leaveType.getAdvanceNoticeDays() != null && leaveType.getAdvanceNoticeDays() > 0) {
            LocalDate fromDate = LocalDate.parse(leaveRequest.getFromDate());
            long daysNotice = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), fromDate);
            if (daysNotice < leaveType.getAdvanceNoticeDays()) {
                // Log warning but don't block - manager can approve emergency cases
                // For strict enforcement, uncomment the throw below
                // throw new IllegalArgumentException(
                //     leaveType.getTypeName() + " requires at least " + 
                //     leaveType.getAdvanceNoticeDays() + " days advance notice");
            }
        }
        
        // Validate max leaves per month (e.g., for menstrual leave)
        if (leaveType.getMaxPerMonth() != null && leaveType.getMaxPerMonth() > 0) {
            LocalDate fromDate = LocalDate.parse(leaveRequest.getFromDate());
            int month = fromDate.getMonthValue();
            int year = fromDate.getYear();
            
            // Count existing approved/pending leaves for this type in this month
            long existingLeavesThisMonth = leaveRepository.findAll().stream()
                .filter(leave -> leave.getLeaveTypeId() != null && 
                                leave.getLeaveTypeId().equals(leaveType.getLeaveTypeId()) &&
                                leave.getEmployeeId().equals(employee.getEmployeeId()) &&
                                leave.getFromDate().getMonthValue() == month &&
                                leave.getFromDate().getYear() == year &&
                                !"Rejected".equalsIgnoreCase(leave.getLeaveStatus()) &&
                                !"Cancelled".equalsIgnoreCase(leave.getLeaveStatus()))
                .count();
            
            if (existingLeavesThisMonth >= leaveType.getMaxPerMonth()) {
                throw new IllegalArgumentException(
                    "Maximum " + leaveType.getMaxPerMonth() + " " + leaveType.getTypeName() + 
                    " allowed per month. You have already used this limit.");
            }
        }
        
        // Validate leave balance
        EmployeeLeaveBalance balance = employeeLeaveBalanceRepository
            .findByEmployeeIdAndLeaveTypeId(employee.getEmployeeId(), leaveType.getLeaveTypeId())
            .orElse(null);
        
        if (balance != null) {
            double availableBalance = balance.getBalanceDays() - balance.getUsedDays();
            if (leaveRequest.getNumberOfDays() != null && leaveRequest.getNumberOfDays() > availableBalance) {
                throw new IllegalArgumentException(
                    "Insufficient leave balance. Available: " + availableBalance + 
                    " days, Requested: " + leaveRequest.getNumberOfDays() + " days");
            }
        }
    }
}
