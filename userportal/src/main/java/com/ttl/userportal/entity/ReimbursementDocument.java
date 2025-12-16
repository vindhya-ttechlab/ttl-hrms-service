package com.ttl.userportal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing documents/receipts attached to a reimbursement claim
 * Supports multiple documents per reimbursement (receipts, invoices, etc.)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reimbursement_documents")
public class ReimbursementDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    private Long documentId;

    @Column(name = "reimbursement_id", nullable = false)
    private Long reimbursementId;

    @Column(name = "document_name", nullable = false, length = 255)
    private String documentName; // Original filename

    @Column(name = "document_path", nullable = false, length = 500)
    private String documentPath; // Server file path

    @Column(name = "document_url", nullable = false, length = 500)
    private String documentUrl; // Accessible URL

    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType; // MIME type (image/jpeg, application/pdf)

    @Column(name = "document_size", nullable = false)
    private Long documentSize; // Size in bytes

    @Column(name = "document_category", length = 50)
    private String documentCategory;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Column(name = "verified_by")
    private Integer verifiedBy;

    @Column(name = "verified_date")
    private LocalDateTime verifiedDate;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "updated_at",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    // Relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reimbursement_id", insertable = false, updatable = false)
    private Reimbursement reimbursement;

    /**
     * Enum for document categories
     */
    public enum DocumentCategory {
        RECEIPT,        // Payment receipt
        INVOICE,        // Invoice/Bill
        TICKET,         // Travel ticket
        BOARDING_PASS,  // Boarding pass
        HOTEL_VOUCHER,  // Hotel booking
        OTHER           // Other supporting documents
    }
}

