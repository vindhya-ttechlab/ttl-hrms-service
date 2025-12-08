package com.ttl.userportal.dto;

import lombok.Data;
import java.util.List;

@Data
public class LoginResponse
{
    private String email;
    private String name;
    private String phoneNumber;
    private String token;
    private Integer userId;
    private List<String> roles; // List of role names for the user
    private List<Integer> roleIds; // List of role IDs for the user
    private Integer primaryRoleId; // Primary role ID (first role, default: 1 for Employee)
}
