package com.ttl.userportal.controller;

import com.ttl.userportal.config.CurrentUser;
import com.ttl.userportal.dto.LeaveRequestDTO;
import com.ttl.userportal.service.LeaveService;
import com.ttl.userportal.util.model.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RestController
@RequestMapping("/leaves")
@CrossOrigin(origins = "http://localhost:3000")
public class LeaveController
{
    @Autowired
    private LeaveService leaveService;

    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>>saveOrUpdate(@RequestBody LeaveRequestDTO leaveRequest, @CurrentUser UserDetails userDetails)
    {
        Map<String,Object>response=new HashMap<>();
        try {

            leaveService.saveOrUpdateLeaveRequest(leaveRequest,userDetails);
            response.put("Data", "Leave Request Saved Successfully");
            return ResponseEntity.ok(response);
         } catch (Exception e) {
            response.put("Data", "Leave Request Save Failed");
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/upcoming-leaves")
    public ResponseEntity<?> getUpcomingLeaveData(@CurrentUser UserDetails userDetails) {
        try {
            List<LeaveRequestDTO> response = leaveService.upcomingLeaves(userDetails.getUser_name(), userDetails);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Log the exception for debugging
            e.printStackTrace();

            // Return a proper error response
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch upcoming leaves");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/past-leaves")
    public ResponseEntity<List<LeaveRequestDTO>>getPastLeaveData(@CurrentUser UserDetails userDetails)
    {
        return ResponseEntity.ok(leaveService.pastLeaves(userDetails));

    }

    @GetMapping("/all-leaves")
    public ResponseEntity<?> getLeaveData(@CurrentUser UserDetails userDetails) {
        try {
            List<LeaveRequestDTO> response = leaveService.getLeaves(userDetails);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Log the exception for debugging
            e.printStackTrace();

            // Return a proper error response
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch upcoming leaves");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @PostMapping("/approve-leave")
    public ResponseEntity<Map<String, Object>>approveLeave(@RequestBody LeaveRequestDTO leaveRequest, @CurrentUser UserDetails userDetails)
    {
        Map<String,Object>response=new HashMap<>();
        try {

            leaveService.approveLeaveRequest(leaveRequest,userDetails);
            response.put("Data", "Leave Request Saved Successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("Data", "Leave Request Save Failed");
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/reject-leave")
    public ResponseEntity<Map<String, Object>>rejectLeave(@RequestBody LeaveRequestDTO leaveRequest, @CurrentUser UserDetails userDetails)
    {
        Map<String,Object>response=new HashMap<>();
        try {

            leaveService.rejectLeaveRequest(leaveRequest,userDetails);
            response.put("Data", "Leave Request Rejected Successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("Data", "Leave Request Save Failed");
            return ResponseEntity.ok(response);
        }
    }

}
