package com.ttl.userportal.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * UserDetailsDTO - Data Transfer Object for user details
 * Contains essential user information needed for various operations
 */
@Data
@Getter
@Setter
public class UserDetailsDTO {

    private Integer userId;  // Keep as Integer for database compatibility
    private String userName;
    private String email;
    private String fullName;
    private String department;
    private String position;
    private Integer managerId;
}
