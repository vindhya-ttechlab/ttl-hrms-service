package com.ttl.userportal.mapper;

import com.ttl.userportal.dto.ReimbursementDTO;
import com.ttl.userportal.dto.ReimbursementDocumentDTO;
import com.ttl.userportal.entity.Reimbursement;
import com.ttl.userportal.entity.ReimbursementDocument;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReimbursementMapper {

    public ReimbursementDTO mapBase(Reimbursement r) {
        ReimbursementDTO dto = new ReimbursementDTO();

        dto.setReimbursementId(r.getReimbursementId());
        dto.setUserId(r.getUserId());
        dto.setCategoryId(r.getCategoryId());
        dto.setTitle(r.getTitle());
        dto.setDescription(r.getDescription());
        dto.setAmount(r.getAmount());
        dto.setCurrency(r.getCurrency());
        dto.setExpenseDate(r.getExpenseDate());
        dto.setMerchantName(r.getMerchantName());
        dto.setPaymentMethod(r.getPaymentMethod());
        dto.setStatus(r.getStatus() == null ? null : r.getStatus().name());
        dto.setApproverId(r.getApproverId());
        dto.setApproverComment(r.getApproverComment());
        dto.setApprovedAmount(r.getApprovedAmount());
        dto.setAppliedDate(r.getAppliedDate());
        dto.setApprovedDate(r.getApprovedDate());
        dto.setPaidDate(r.getPaidDate());
        dto.setPaymentReference(r.getPaymentReference());
        dto.setRejectionReason(r.getRejectionReason());
        dto.setIsActive(r.getIsActive());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());
        dto.setHrApproverId(r.getHrApproverId());
        dto.setHrApprovedDate(r.getHrApprovedDate());
        dto.setHrComment(r.getHrComment());

        return dto;
    }

    public ReimbursementDocumentDTO toDocumentDTO(ReimbursementDocument d) {
        return new ReimbursementDocumentDTO(
                d.getDocumentId(),
                d.getReimbursementId(),
                d.getDocumentName(),
                d.getDocumentUrl(),
                d.getDocumentType(),
                d.getDocumentSize(),
                d.getDocumentCategory(),
                d.getIsVerified(),
                d.getVerifiedBy(),
                d.getVerifiedDate(),
                d.getUploadedAt()
        );
    }

    public List<ReimbursementDTO> toDtoList(List<Reimbursement> list) {
        return list.stream().map(this::mapBase).toList();
    }
}