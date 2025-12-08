package com.ttl.userportal.controller;

import com.ttl.userportal.config.CurrentUser;
import com.ttl.userportal.dto.AttendanceRequestDTO;
import com.ttl.userportal.service.AttendanceService;
import com.ttl.userportal.util.model.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/attendance")
@CrossOrigin(origins = "http://localhost:3000")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveOrUpdate(@RequestBody AttendanceRequestDTO attendanceRequest, @CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            attendanceService.saveOrUpdateAttendance(attendanceRequest, userDetails);
            response.put("Data", "Attendance Saved Successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("Data", "Attendance Save Failed");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getAttendanceByUser(@PathVariable Long userId, @CurrentUser UserDetails userDetails) {
        try {
            List<AttendanceRequestDTO> response = attendanceService.getAttendanceByUser(userId, userDetails);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch attendance data");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/month/{year}/{month}")
    public ResponseEntity<?> getAttendanceByMonth(@PathVariable int year, @PathVariable int month, @CurrentUser UserDetails userDetails) {
        try {
            List<AttendanceRequestDTO> response = attendanceService.getAttendanceByMonth(year, month, userDetails);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch monthly attendance data");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
