package com.ttl.userportal.dto;

import lombok.Data;

import java.time.LocalDateTime;


@Data
public class EmailDetailsDTO
{
    private String senderEmail;
    private String receiverEmail;
    private String subject;
    private String message;
    private LocalDateTime sentDateTime;
}
