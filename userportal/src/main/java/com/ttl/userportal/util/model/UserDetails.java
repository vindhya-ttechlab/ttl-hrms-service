package com.ttl.userportal.util.model;

import com.ttl.userportal.entity.Role;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class UserDetails implements Serializable {
    private Long user_id;
    private String user_name;
//    private OrganizationDetails organization;
    private String first_name;
    private String middle_name;
    private String last_name;
    private List<Role> role;
    private List<Integer> roleIds; // List of role IDs for the user
    private Integer primaryRoleId; // Primary role ID (first role, default: 1 for Employee)
    private String designation;
    private String department;
    private String employee;
    private Long reporting_manager_id;
    private String base_branch_code;
    private String contact_no;
    private String email;
    private String contact_address;
    private String city;
    private String state;
    private String postal_code;
    private String country;
    private String address_latitude;
    private String address_longitude;
    private String id_type;
    private String id_number;
    private String gender;
    private String preferred_language;
    private String bank_account_name;
    private String bank_account_no;
    private String bank_account_type;
    private String bank_name;
    private String bank_ifsc_code;
    private String dra_unique_registration_no;
    private String user_security_profile;
    private String created_by;
    private String last_modified_by;
    private String user_status;
    private String agency_access;
    private String access_level;
    private String source;
    private String secChUaHeader;
    private String secChUaPlatformHeader;
    private String userAgentHeader;
    private String deviceId;
}