package com.ttl.userportal.repository;

import com.ttl.userportal.entity.ReimbursementDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReimbursementDocumentRepository extends JpaRepository<ReimbursementDocument, Long> {
    List<ReimbursementDocument> findByReimbursementIdOrderByUploadedAtDesc(Long reimbursementId);
}

