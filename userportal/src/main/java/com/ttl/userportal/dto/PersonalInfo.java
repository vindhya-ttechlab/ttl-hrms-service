package com.ttl.userportal.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class PersonalInfo
{
    private String id;
    private String name;
    private String employeeId;
    private String email;
    private String phone;
    private String location;
    private String dateOfBirth;   // Could be LocalDate if you want strict date handling
    private String emergencyContact;
    private String emergencyPhone;
    private String address;
    private String profileImage;
    private String status;
}
