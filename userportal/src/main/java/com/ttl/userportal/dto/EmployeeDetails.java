package com.ttl.userportal.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class EmployeeDetails
{
    private Integer id;
    private String name;
    private String email;
    private String phone;
    private String designation;
    private String department;
    private LocalDate joiningDate;
    private String employeeCode;

    // Bank Details
    private String bankName;
    private String accountNo;
    private String ifscCode;
    private String branchName;

    // Document Details
    private List<DocumentDTO> documents;

    // Status or Meta Info
    private Boolean isActive;
    private String createdBy;
    private LocalDate createdDate;
}
