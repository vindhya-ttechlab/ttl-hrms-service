package com.ttl.userportal.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public  class ManagerDTO {
    private Integer id;
    private String name;
    private String position;
    private String email;
    private String phone;
    private String profileImage;
}