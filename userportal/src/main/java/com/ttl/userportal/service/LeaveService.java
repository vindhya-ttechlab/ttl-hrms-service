package com.ttl.userportal.service;

import com.ttl.userportal.dto.EmailDetailsDTO;
import com.ttl.userportal.dto.LeaveRequestDTO;
import com.ttl.userportal.entity.*;
import com.ttl.userportal.mapper.LeaveMapper;
import com.ttl.userportal.repository.*;
import com.ttl.userportal.util.model.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
    //save LeaveRequest
    public void saveOrUpdateLeaveRequest(LeaveRequestDTO leaveRequest, UserDetails userDetails)
    {
        LocalDateTime now=LocalDateTime.now();
        // Validate that leave request is not null
        if(leaveRequest == null)
        {
            throw new IllegalArgumentException("Leave request cannot be null");
        }
        Integer userId = Math.toIntExact(userDetails.getUser_id());
        Users users = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User Not found"));
        Employee employee = employeeRepository.findByEmail(users.getEmail())
                .orElseThrow(() -> new RuntimeException("Employee not found for user email: " + users.getEmail()));

        Integer managerEmployeeId = employee.getManagerId();
        Employee managerEmployee = null;
        if (managerEmployeeId != null) {
            managerEmployee = employeeRepository.findById(managerEmployeeId)
                    .orElseThrow(() -> new RuntimeException("Manager employee not found"));
        }
        if (managerEmployee != null) {
            EmailDetailsDTO emailDetailsDTO = new EmailDetailsDTO();
            emailDetailsDTO.setReceiverEmail(managerEmployee.getEmail());
            emailDetailsDTO.setSenderEmail(employee.getEmail());
            emailDetailsDTO.setMessage(leaveRequest.getReason());
            LocalDate fromDate = LocalDate.parse(leaveRequest.getFromDate());
            LocalDate endDate = LocalDate.parse(leaveRequest.getToDate());
            emailDetailsDTO.setSubject(leaveRequest.getType() + " Request from " + fromDate + " to " + endDate);
            emailDetailsDTO.setSentDateTime(now);
            emailNotificationService.sendEmail(emailDetailsDTO);
        }
        
        if(leaveRequest.getId() == null) {
            // Create new leave using mapper
            LeaveEntity leaveEntity = leaveMapper.mapToLeaveEntity(
                leaveRequest, 
                employee.getEmployeeId(), 
                managerEmployeeId, 
                now
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
            String oldStatus = leaveEntity.getLeaveStatus();
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
                EmailDetailsDTO emailDetailsDTO = new EmailDetailsDTO();
                emailDetailsDTO.setReceiverEmail(employee.getEmail());
                emailDetailsDTO.setSenderEmail(managerEmployee.getEmail());
                emailDetailsDTO.setMessage(leaveRequestDTO.getComment());
                emailDetailsDTO.setSubject("Approved "+leaveEntity.getType()+" Request from "+leaveEntity.getFromDate()+" to "+leaveEntity.getToDate());
                emailDetailsDTO.setSentDateTime(now);
                emailNotificationService.sendEmail(emailDetailsDTO);
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
            String oldStatus = leaveEntity.getLeaveStatus();
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
                EmailDetailsDTO emailDetailsDTO = new EmailDetailsDTO();
                emailDetailsDTO.setReceiverEmail(employee.getEmail());
                emailDetailsDTO.setSenderEmail(managerEmployee.getEmail());
                emailDetailsDTO.setMessage(leaveRequestDTO.getComment());
                emailDetailsDTO.setSubject("Rejected "+leaveEntity.getType()+" Request from "+leaveEntity.getFromDate()+" to "+leaveEntity.getToDate());
                emailDetailsDTO.setSentDateTime(now);
                emailNotificationService.sendEmail(emailDetailsDTO);
            }
        }
    }
}
