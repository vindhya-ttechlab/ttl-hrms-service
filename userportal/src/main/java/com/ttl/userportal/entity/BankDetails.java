package com.ttl.userportal.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "BankDetails")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class BankDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "ifsc_code")
    private String ifscCode;

    @Column(name = "branch_name")
    private String branchName;
}
