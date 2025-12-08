package com.ttl.userportal.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateUserRequest {
    private String empCode;
    private String name;
    private String email;
    private String password;
    private String phone;
    private String location;
    private LocalDate dateOfBirth;
    private String emergencyContact;
    private String emergencyPhone;
    private String address;
    private String position;
    private String department;
    private LocalDate joinDate;
    private String experience;
    private String education;
    private String team;
    private Integer manager;
    private String skills;
    private String languages;
    private String achievement;
}
