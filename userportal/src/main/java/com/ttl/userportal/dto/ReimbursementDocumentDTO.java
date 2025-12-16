package com.ttl.userportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReimbursementDocumentDTO {
    private Long documentId;
    private Long reimbursementId;
    private String documentName;
    private String documentUrl;
    private String documentType;
    private Long documentSize;
    private String documentCategory; // RECEIPT, INVOICE, TICKET, etc.
    private Boolean isVerified;
    private Integer verifiedBy;
    private LocalDateTime verifiedDate;
    private LocalDateTime uploadedAt;

}

