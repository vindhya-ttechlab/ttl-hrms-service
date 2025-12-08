package com.ttl.userportal.service;

import com.ttl.userportal.dto.EmailDetailsDTO;
import com.ttl.userportal.dto.LeaveRequestDTO;
import com.ttl.userportal.dto.UserDetailsDTO;
import com.ttl.userportal.entity.LeaveEntity;
import com.ttl.userportal.entity.Users;
import com.ttl.userportal.repository.LeaveRepository;
import com.ttl.userportal.repository.UserRepository;
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
    EmailNotificationService emailNotificationService;
    //save LeaveRequest
    public void saveOrUpdateLeaveRequest(LeaveRequestDTO leaveRequest, UserDetails userDetails)
    {
        LocalDateTime now=LocalDateTime.now();
        // Validate that leave request is not null
        if(leaveRequest == null)
        {
            throw new IllegalArgumentException("Leave request cannot be null");
        }
        String userName = userDetails.getUser_name();
        Integer userId = Math.toIntExact(userDetails.getUser_id());

        LeaveEntity leaveEntity = new LeaveEntity();
        LocalDate fromDate= LocalDate.parse(leaveRequest.getFromDate());
        LocalDate endDate= LocalDate.parse(leaveRequest.getToDate());

        EmailDetailsDTO emailDetailsDTO=new EmailDetailsDTO();

            Users users=userRepository.findById(userId).orElseThrow(()->new RuntimeException("User Not found"));
            Integer managerId=users.getManager();
            Users manager = userRepository.findById(managerId)
                    .orElseThrow(() -> new RuntimeException("Manager not found"));
            emailDetailsDTO.setReceiverEmail(manager.getEmail());
            emailDetailsDTO.setSenderEmail(users.getEmail());
            emailDetailsDTO.setMessage(leaveRequest.getReason());
            emailDetailsDTO.setSubject(leaveRequest.getType()+"  Request"+" from "+fromDate+" to "+endDate);
            emailDetailsDTO.setSentDateTime(now);
            emailNotificationService.sendEmail(emailDetailsDTO);
        
        if(leaveRequest.getId()==null)
        {
            leaveEntity.setFromDate(fromDate);
            leaveEntity.setToDate(endDate);
            leaveEntity.setReason(leaveRequest.getReason());
            leaveEntity.setType(leaveRequest.getType());
            leaveEntity.setUserId(userId);
            leaveEntity.setApprover(manager.getId());
            leaveEntity.setAppliedDate(now);
            leaveEntity.setApprovedDate(leaveRequest.getApprovedDate());
            leaveEntity.setIsActive(true);
            // Set default status for new leave requests
            leaveEntity.setLeaveStatus(leaveRequest.getLeaveStatus() != null ? leaveRequest.getLeaveStatus() : "Pending");
            // Calculate and set number of days
            leaveEntity.setNumberOfDays(leaveRequest.getNumberOfDays());

            leaveRepository.save(leaveEntity);


        }else
        {
            leaveEntity=leaveRepository.findByIdAndIsActive(leaveRequest.getId(),true);
            if (leaveEntity == null) {
                throw new IllegalArgumentException("Leave request with ID " + leaveRequest.getId() + " not found or inactive");
            }
            
            leaveEntity.setFromDate(fromDate);
            leaveEntity.setToDate(endDate);
            leaveEntity.setReason(leaveRequest.getReason());
            leaveEntity.setType(leaveRequest.getType());
            leaveEntity.setUserId(userId);
            leaveEntity.setApprover(manager.getId());
            leaveEntity.setAppliedDate(leaveRequest.getAppliedDate());
            leaveEntity.setApprovedDate(leaveRequest.getApprovedDate());
            leaveEntity.setIsActive(true);
            // Update leave status if provided
            if(leaveRequest.getLeaveStatus() != null) {
                leaveEntity.setLeaveStatus(leaveRequest.getLeaveStatus());
            }
            leaveEntity.setNumberOfDays(leaveRequest.getNumberOfDays());

            leaveRepository.save(leaveEntity);
        }
    }

    //getUpcomingLeaveData
    public List<LeaveRequestDTO> upcomingLeaves(String userName,UserDetails userDetails)
    {

        LocalDate today = LocalDate.now();
//        Users users=userRepository.findByEmail(userName);
        Integer userId= Math.toIntExact(userDetails.getUser_id());

        List<LeaveEntity> upcomingLeaves = leaveRepository.listOfUpcomingLeaves(today,userId);

        // Map entities to DTOs manually
        return upcomingLeaves.stream()
                .map(leaveEntity -> {
                    LeaveRequestDTO dto = new LeaveRequestDTO();
                    dto.setId(leaveEntity.getId());
                    dto.setFromDate(String.valueOf(leaveEntity.getFromDate()));
                    dto.setToDate(String.valueOf(leaveEntity.getToDate()));
                    dto.setReason(leaveEntity.getReason());
                    dto.setType(leaveEntity.getType());
                    dto.setUserId(Math.toIntExact(userDetails.getUser_id())); // Set actual Integer userId
                    dto.setApprover(leaveEntity.getApprover());
                    dto.setAppliedDate(leaveEntity.getAppliedDate());
                    dto.setApprovedDate(leaveEntity.getApprovedDate());
                    dto.setIsActive(leaveEntity.getIsActive());
                    dto.setLeaveStatus(leaveEntity.getLeaveStatus());
                    dto.setNumberOfDays(leaveEntity.getNumberOfDays());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    //past leaves
    public List<LeaveRequestDTO> pastLeaves(UserDetails userDetails)
    {

        LocalDate today = LocalDate.now();
//        Users users=userRepository.findByEmail(userName);
        Integer userId= Math.toIntExact(userDetails.getUser_id());

        List<LeaveEntity> upcomingLeaves = leaveRepository.listOfPastLeaves(today,userId);

        // Map entities to DTOs manually
        return upcomingLeaves.stream()
                .map(leaveEntity -> {
                    LeaveRequestDTO dto = new LeaveRequestDTO();
                    dto.setId(leaveEntity.getId());
                    dto.setFromDate(String.valueOf(leaveEntity.getFromDate()));
                    dto.setToDate(String.valueOf(leaveEntity.getToDate()));
                    dto.setReason(leaveEntity.getReason());
                    dto.setType(leaveEntity.getType());
                    dto.setUserId(userId); // Set actual Integer userId
                    dto.setApprover(leaveEntity.getApprover());
                    dto.setAppliedDate(leaveEntity.getAppliedDate());
                    dto.setApprovedDate(leaveEntity.getApprovedDate());
                    dto.setIsActive(leaveEntity.getIsActive());
                    dto.setLeaveStatus(leaveEntity.getLeaveStatus());
                    dto.setNumberOfDays(leaveEntity.getNumberOfDays());
                    return dto;
                })
                .collect(Collectors.toList());
    }


    public List<LeaveRequestDTO> getLeaves(UserDetails userDetails)
    {

        LocalDate today = LocalDate.now();
        int currentYear = LocalDate.now().getYear();
        Integer userId= Math.toIntExact(userDetails.getUser_id());

        List<LeaveEntity> leaves = leaveRepository.findByApprover(currentYear,userId);

        // Map entities to DTOs manually
        return leaves.stream()
                .map(leaveEntity -> {
                    Users users=userRepository.findById(leaveEntity.getUserId()).orElseThrow(()->new RuntimeException("User Not found"));
                    LeaveRequestDTO dto = new LeaveRequestDTO();
                    dto.setId(leaveEntity.getId());
                    dto.setFromDate(String.valueOf(leaveEntity.getFromDate()));
                    dto.setToDate(String.valueOf(leaveEntity.getToDate()));
                    dto.setReason(leaveEntity.getReason());
                    dto.setType(leaveEntity.getType());
                    dto.setUserId(users.getId()); // Set actual Integer userId
                    dto.setApprover(leaveEntity.getApprover());
                    dto.setAppliedDate(leaveEntity.getAppliedDate());
                    dto.setApprovedDate(leaveEntity.getApprovedDate());
                    dto.setIsActive(leaveEntity.getIsActive());
                    dto.setLeaveStatus(leaveEntity.getLeaveStatus());
                    dto.setNumberOfDays(leaveEntity.getNumberOfDays());
                    dto.setUserName(users.getName());

                    return dto;
                })
                .collect(Collectors.toList());
    }


    public void approveLeaveRequest(LeaveRequestDTO leaveRequestDTO,UserDetails userDetails)
    {
        LocalDateTime now=LocalDateTime.now();
        if(leaveRequestDTO.getId()!=null)
        {
            LeaveEntity leaveEntity=leaveRepository.findById(leaveRequestDTO.getId()).orElseThrow(()-> new RuntimeException("Leave Request is Empty"));
            leaveEntity.setLeaveStatus("Approved");
            leaveEntity.setApprovedDate(now);
            leaveEntity.setManagerComment(leaveRequestDTO.getComment());
            leaveRepository.save(leaveEntity);


            EmailDetailsDTO emailDetailsDTO=new EmailDetailsDTO();

            Users users=userRepository.findById(userDetails.getUser_id());
            Integer managerId=leaveEntity.getApprover();
            Users manager = userRepository.findById(managerId)
                    .orElseThrow(() -> new RuntimeException("Manager not found"));
            emailDetailsDTO.setReceiverEmail(users.getEmail());
            emailDetailsDTO.setSenderEmail(manager.getEmail());
            emailDetailsDTO.setMessage(leaveRequestDTO.getComment());
            emailDetailsDTO.setSubject("Approved "+leaveEntity.getType()+"  Request"+" from "+leaveEntity.getFromDate()+" to "+leaveEntity.getToDate());
            emailDetailsDTO.setSentDateTime(now);
            emailNotificationService.sendEmail(emailDetailsDTO);
        }
    }

    public void rejectLeaveRequest(LeaveRequestDTO leaveRequestDTO,UserDetails userDetails)
    {
        LocalDateTime now=LocalDateTime.now();
        if(leaveRequestDTO.getId()!=null)
        {
            LeaveEntity leaveEntity=leaveRepository.findById(leaveRequestDTO.getId()).orElseThrow(()-> new RuntimeException("Leave Request is Empty"));
            leaveEntity.setLeaveStatus("Rejected");
            leaveEntity.setApprovedDate(now);
            leaveEntity.setManagerComment(leaveRequestDTO.getComment());
            leaveRepository.save(leaveEntity);


            EmailDetailsDTO emailDetailsDTO=new EmailDetailsDTO();

            Users users=userRepository.findById(userDetails.getUser_id());
            Integer managerId=leaveEntity.getApprover();
            Users manager = userRepository.findById(managerId)
                    .orElseThrow(() -> new RuntimeException("Manager not found"));
            emailDetailsDTO.setReceiverEmail(users.getEmail());
            emailDetailsDTO.setSenderEmail(manager.getEmail());
            emailDetailsDTO.setMessage(leaveRequestDTO.getComment());
            emailDetailsDTO.setSubject("Rejected "+leaveEntity.getType()+"  Request"+" from "+leaveEntity.getFromDate()+" to "+leaveEntity.getToDate());
            emailDetailsDTO.setSentDateTime(now);
            emailNotificationService.sendEmail(emailDetailsDTO);
        }
    }
}
