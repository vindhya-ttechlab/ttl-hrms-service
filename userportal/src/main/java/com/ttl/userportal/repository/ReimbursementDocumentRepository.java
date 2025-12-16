package com.ttl.userportal.repository;

import com.ttl.userportal.entity.ReimbursementDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ReimbursementDocumentRepository extends JpaRepository<ReimbursementDocument, Long> {
    
    // Find all documents for a reimbursement
    List<ReimbursementDocument> findByReimbursementIdOrderByUploadedAtDesc(Long reimbursementId);
    
    // Find documents by category
    List<ReimbursementDocument> findByReimbursementIdAndDocumentCategory(
            Long reimbursementId, ReimbursementDocument.DocumentCategory category);
    
    // Count documents for a reimbursement
    Long countByReimbursementId(Long reimbursementId);
    
    // Delete all documents for a reimbursement
    @Modifying
    @Transactional
    void deleteByReimbursementId(Long reimbursementId);
    
    // Find unverified documents
    @Query("SELECT d FROM ReimbursementDocument d WHERE d.isVerified = false " +
           "ORDER BY d.uploadedAt ASC")
    List<ReimbursementDocument> findUnverifiedDocuments();
}

