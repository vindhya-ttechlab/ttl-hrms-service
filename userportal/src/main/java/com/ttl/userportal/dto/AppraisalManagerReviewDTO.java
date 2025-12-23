package com.ttl.userportal.dto;

import lombok.Data;

import java.util.List;

@Data
public class AppraisalManagerReviewDTO {
    private Long reviewId;
    private Long employeeId; //not employee table yet it's users table
    private String employeeName;
    private EmployeeSelfAssessmentDTO selfAssessmentDTO;
    private String reviewText;
}
