package com.ttl.userportal.service;

import com.ttl.userportal.dto.*;
import com.ttl.userportal.util.model.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ReimbursementService {

    List<ReimbursementCategoryDTO> getAllCategories();

    ReimbursementCategoryDTO createCategory(ReimbursementCategoryDTO categoryDTO, UserDetails userDetails);

    ReimbursementDTO createOrUpdateReimbursement(ReimbursementRequestDTO request, UserDetails userDetails);

    List<ReimbursementDTO> getMyReimbursements(UserDetails userDetails);

    List<ReimbursementDTO> getMyReimbursementsByStatus(String status, UserDetails userDetails);

    ReimbursementDTO getReimbursementById(Long id, UserDetails userDetails);

    ReimbursementSummaryDTO getReimbursementSummary(UserDetails userDetails);

    ReimbursementDTO cancelReimbursement(Long id, UserDetails userDetails);

    List<ReimbursementDTO> getPendingApprovals(UserDetails userDetails);

    List<ReimbursementDTO> getPendingManagerApprovals(UserDetails user);

    List<ReimbursementDTO> getPendingHrApprovals();

    ReimbursementDTO approveOrReject(ReimbursementApprovalDTO approval, UserDetails userDetails);

    ReimbursementDocumentDTO uploadDocument(Long reimbursementId, MultipartFile file, String category, UserDetails userDetails) throws IOException;

    boolean canAccessReimbursement(Long reimbursementId, UserDetails userDetails);

    void deleteDocument(Long documentId, UserDetails userDetails);

    ReimbursementDTO hrApproveOrReject(ReimbursementApprovalDTO approval, UserDetails userDetails);
}
