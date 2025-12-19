package com.ttl.userportal.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "email_details")
public class EmailDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String senderEmail;
    private String receiverEmail;
    
    @Column(length = 500)
    private String subject;
    
    @Column(columnDefinition = "LONGTEXT")
    private String message;
    
    private LocalDateTime sentDateTime;

}
