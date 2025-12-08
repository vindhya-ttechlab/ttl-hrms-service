package com.ttl.userportal.dto;

import com.ttl.userportal.entity.Users;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDTO {
    private Long id;
    private String documentName;
    private String documentType;
    private String documentUrl;
    private Boolean verified;
    private Integer userId;

}
