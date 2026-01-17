package com.ttl.userportal.service.impl;

import com.ttl.userportal.dto.*;
import com.ttl.userportal.entity.*;
import com.ttl.userportal.enums.ApprovalAction;
import com.ttl.userportal.enums.ReimbursementStatus;
import com.ttl.userportal.mapper.ReimbursementCategoryMapper;
import com.ttl.userportal.mapper.ReimbursementMapper;
import com.ttl.userportal.notification.enums.ReimbursementNotificationType;
import com.ttl.userportal.notification.service.ReimbursementNotificationService;
import com.ttl.userportal.repository.ReimbursementCategoryRepository;
import com.ttl.userportal.repository.ReimbursementDocumentRepository;
import com.ttl.userportal.repository.ReimbursementRepository;
import com.ttl.userportal.repository.UserRepository;
import com.ttl.userportal.service.ReimbursementAuthorizationService;
import com.ttl.userportal.service.ReimbursementService;
import com.ttl.userportal.service.storage.ReimbursementFileStorageService;
import com.ttl.userportal.service.storage.model.StoredFile;
import com.ttl.userportal.util.model.UserDetails;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
@Service
public class ReimbursementServiceImpl implements ReimbursementService {

    private final ReimbursementCategoryRepository categoryRepository;
    private final ReimbursementCategoryMapper categoryMapper;
    private final ReimbursementRepository reimbursementRepository;

    private final UserRepository userRepository;
    private final ReimbursementDocumentRepository documentRepository;
    private final ReimbursementMapper reimbursementMapper;

    private final ReimbursementNotificationService notificationService;

    private final ReimbursementFileStorageService fileStorageService;

    private final ReimbursementAuthorizationService authorizationService;

    public ReimbursementServiceImpl(ReimbursementCategoryRepository categoryRepository, ReimbursementCategoryMapper categoryMapper, ReimbursementRepository reimbursementRepository, UserRepository userRepository, ReimbursementDocumentRepository documentRepository, ReimbursementMapper reimbursementMapper, ReimbursementNotificationService notificationService, ReimbursementFileStorageService fileStorageService, ReimbursementAuthorizationService authorizationService) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.reimbursementRepository = reimbursementRepository;
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
        this.reimbursementMapper = reimbursementMapper;
        this.notificationService = notificationService;
        this.fileStorageService = fileStorageService;
        this.authorizationService = authorizationService;
    }

    @Value("${app.base-url}")
    private String baseUrl;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp", "application/pdf");

    @Override
    public List<ReimbursementCategoryDTO> getAllCategories() {
        return categoryRepository.findByIsActiveTrueOrderByCategoryNameAsc().stream().map(categoryMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReimbursementCategoryDTO createCategory(ReimbursementCategoryDTO categoryDTO, UserDetails userDetails) {
        if (categoryRepository.existsByCategoryName(categoryDTO.getCategoryName())) {
            throw new IllegalArgumentException("Category with name " + categoryDTO.getCategoryName() + " already exists.");
        }

        ReimbursementCategory category = categoryMapper.toEntity(categoryDTO);
        ReimbursementCategory savedCategory = categoryRepository.save(category);

        return categoryMapper.toDto(savedCategory);
    }

    @Override
    public ReimbursementDTO createOrUpdateReimbursement(ReimbursementRequestDTO request, UserDetails userDetails) {

        Integer userId = userDetails.getUser_id().intValue();

        Users user = getUser(userDetails);
        Integer approverId = getApproverDetails(user, request.getManagerId());
        ReimbursementCategory category = getCategory(request.getCategoryId());

        validateAmount(category, request.getAmount());

        boolean isNew = request.getReimbursementId() == null;

        Reimbursement reimbursement = isNew ? createNewReimbursement(user) : getUpdatableableReimbursement(request.getReimbursementId());

        mapRequestToEntity(reimbursement, request, approverId);

        setStatusAndNotify(reimbursement, request.getSubmitForApproval(), user);

        Reimbursement saved = reimbursementRepository.save(reimbursement);
        log.info("Reimbursement {} for user {} has been saved/updated", saved.getReimbursementId(), userId);

        return convertToDto(saved);
    }


    private Users getUser(UserDetails userDetails) {
        return userRepository.findById(userDetails.getUser_id().intValue()).orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userDetails.getUser_id()));
    }

    private Integer getApproverDetails(Users user, Integer managerId) {
        Integer resolvedManagerId = managerId != null ? managerId : user.getManager();
        if (resolvedManagerId == null) {
            throw new IllegalArgumentException("No manager assigned. Cannot submit reimbursement.");
        }
        return resolvedManagerId;
    }

    private ReimbursementCategory getCategory(Integer categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() -> new IllegalArgumentException("Reimbursement category not found"));
    }

    private void validateAmount(ReimbursementCategory category, BigDecimal amount) {
        if (category.getMaxAmount() != null && amount.compareTo(category.getMaxAmount()) > 0) {
            throw new IllegalArgumentException("Amount exceeds maximum limit of " + category.getMaxAmount() + " for category: " + category.getCategoryName());
        }
    }

    private Reimbursement createNewReimbursement(Users user) {
        Reimbursement reimbursement = new Reimbursement();
        reimbursement.setUserId(user.getId());
        reimbursement.setAppliedDate(LocalDateTime.now());
        reimbursement.setCreatedAt(LocalDateTime.now());
        reimbursement.setIsActive(true);

        return reimbursement;
    }

    private Reimbursement getUpdatableableReimbursement(Long reimbursementId) {
        Reimbursement reimbursement = reimbursementRepository.findById(reimbursementId).orElseThrow(() -> new IllegalArgumentException("Reimbursement is not found"));

        if (reimbursement.getStatus() != ReimbursementStatus.DRAFT) {
            throw new IllegalArgumentException("Cannot update Reimbursement in " + reimbursement.getStatus() + " status");
        }

        return reimbursement;
    }

    private void mapRequestToEntity(Reimbursement reimbursement, ReimbursementRequestDTO request, Integer approverId) {
        reimbursement.setCategoryId(request.getCategoryId());
        reimbursement.setTitle(request.getTitle());
        reimbursement.setDescription(request.getDescription());
        reimbursement.setAmount(request.getAmount());
        reimbursement.setCurrency(request.getCurrency() != null ? request.getCurrency() : "INR");
        reimbursement.setExpenseDate(request.getExpenseDate());
        reimbursement.setMerchantName(request.getMerchantName());
        reimbursement.setPaymentMethod(request.getPaymentMethod());
        reimbursement.setApproverId(approverId);
        reimbursement.setUpdatedAt(LocalDateTime.now());
    }


    private void setStatusAndNotify(Reimbursement r, Boolean submitForApproval, Users user) {
        if (Boolean.TRUE.equals(submitForApproval)) {
            r.setStatus(ReimbursementStatus.PENDING_MANAGER);

            notificationService.send(r, user, r.getApproverId(), ReimbursementNotificationType.NEW_REQUEST);
        } else {
            r.setStatus(ReimbursementStatus.DRAFT);
        }
    }

    private ReimbursementDTO convertToDto(Reimbursement r) {

        ReimbursementDTO dto = reimbursementMapper.mapBase(r);

        Users user = userRepository.findById(r.getUserId()).orElse(null);
        ReimbursementCategory category = categoryRepository.findById(r.getCategoryId()).orElse(null);

        if (user != null) {
            dto.setUserName(user.getName());
            dto.setUserEmail(user.getEmail());
        }

        if (category != null) {
            dto.setCategoryName(category.getCategoryName());
        }

        dto.setDocuments(documentRepository.findByReimbursementIdOrderByUploadedAtDesc(r.getReimbursementId()).stream().map(reimbursementMapper::toDocumentDTO).toList());

        return dto;
    }

    @Override
    public List<ReimbursementDTO> getMyReimbursements(UserDetails userDetails) {
        Integer userId = userDetails.getUser_id().intValue();

        List<Reimbursement> reimbursements = reimbursementRepository.findByUserIdAndIsActiveTrueOrderByAppliedDateDesc(userId);

        return reimbursements.stream().map(this::convertToDto).toList();
    }


    @Override
    public List<ReimbursementDTO> getMyReimbursementsByStatus(String status, UserDetails userDetails) {
        Integer userId = userDetails.getUser_id().intValue();

        ReimbursementStatus reimbursementStatus;
        try {
            reimbursementStatus = ReimbursementStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid reimbursement status: " + status);
        }

        return reimbursementRepository.findByUserIdAndStatusAndIsActiveTrueOrderByAppliedDateDesc(userId, reimbursementStatus).stream().map(this::convertToDto).toList();
    }

    @Override
    public ReimbursementDTO getReimbursementById(Long reimbursementId, UserDetails userDetails) {
        Reimbursement reimbursement = reimbursementRepository.findById(reimbursementId).orElseThrow(() -> new IllegalArgumentException("Reimbursement not found: " + reimbursementId));

        if (!authorizationService.canAccess(reimbursement, userDetails)) {
            throw new SecurityException("Access denied to reimbursement: " + reimbursementId);
        }

        return convertToDto(reimbursement);
    }


    @Override
    public ReimbursementSummaryDTO getReimbursementSummary(UserDetails userDetails) {

        Integer userId = userDetails.getUser_id().intValue();
        int currentYear = LocalDate.now().getYear();

        List<Reimbursement> reimbursements = reimbursementRepository.findByUserIdAndIsActiveTrueOrderByAppliedDateDesc(userId);

        long pendingCount = 0;
        long approvedCount = 0;
        long rejectedCount = 0;
        BigDecimal pendingAmount = BigDecimal.ZERO;

        for (Reimbursement r : reimbursements) {

            ReimbursementStatus status = r.getStatus();

            switch (status) {

                case PENDING_MANAGER, PENDING_HR -> {
                    pendingCount++;
                    pendingAmount = pendingAmount.add(r.getAmount());
                }

                case APPROVED -> approvedCount++;

                case REJECTED -> rejectedCount++;
            }
        }

        BigDecimal totalApproved = reimbursementRepository.sumApprovedAmountByUserAndYear(userId, currentYear, ReimbursementStatus.APPROVED);

        return new ReimbursementSummaryDTO(pendingCount, approvedCount, rejectedCount, totalApproved != null ? totalApproved : BigDecimal.ZERO, pendingAmount, currentYear);
    }

    @Override
    @Transactional
    public ReimbursementDTO cancelReimbursement(Long reimbursementId, UserDetails userDetails) {
        Integer userId = userDetails.getUser_id().intValue();

        Reimbursement reimbursement = reimbursementRepository.findById(reimbursementId)
                .orElseThrow(() -> new IllegalArgumentException("Reimbursement not found"));

        if (!authorizationService.isOwner(reimbursement, userDetails)) {
            throw new SecurityException("You can only cancel your own reimbursements");
        }

        // Convert status to enum for safe comparison
        ReimbursementStatus status = reimbursement.getStatus();

        if (!(status == ReimbursementStatus.DRAFT || status == ReimbursementStatus.PENDING_MANAGER)) {
            throw new IllegalArgumentException("Cannot cancel reimbursement in status: " + status);
        }

        reimbursement.setStatus(ReimbursementStatus.CANCELLED);
        reimbursement.setUpdatedAt(LocalDateTime.now());

        Reimbursement saved = reimbursementRepository.save(reimbursement);

        log.info("Cancelled reimbursement ID: {} by user: {}", reimbursementId, userId);

        return convertToDto(saved);
    }

    @Override
    @Transactional
    public ReimbursementDTO approveOrReject(ReimbursementApprovalDTO approval, UserDetails userDetails) {
        Integer approverId = userDetails.getUser_id().intValue();
        LocalDateTime now = LocalDateTime.now();

        Reimbursement reimbursement = reimbursementRepository.findById(approval.getReimbursementId()).orElseThrow(() -> new RuntimeException("Reimbursement not found"));

        // Authorization check
        if (!authorizationService.canApproveAsManager(reimbursement, userDetails)) {
            throw new SecurityException("Only assigned manager can approve this reimbursement");
        }

        // State guard
        if (reimbursement.getStatus() != ReimbursementStatus.PENDING_MANAGER) {
            throw new IllegalArgumentException("Reimbursement not pending manager approval");
        }

        ApprovalAction action;
        try {
            action = ApprovalAction.valueOf(approval.getAction().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid action. Use APPROVE or REJECT");
        }

        switch (action) {

            case APPROVE -> {
                reimbursement.setStatus(ReimbursementStatus.PENDING_HR);
                reimbursement.setApprovedAmount(approval.getApprovedAmount() != null ? approval.getApprovedAmount() : reimbursement.getAmount());
                reimbursement.setApprovedDate(now);
                reimbursement.setApproverComment(approval.getComment());

                notificationService.send(reimbursement, null, null, ReimbursementNotificationType.APPROVED);
            }

            case REJECT -> {
                reimbursement.setStatus(ReimbursementStatus.REJECTED);
                reimbursement.setApprovedDate(now);
                reimbursement.setApproverComment(approval.getComment());
                reimbursement.setRejectionReason(approval.getRejectionReason());

                notificationService.send(reimbursement, null, null, ReimbursementNotificationType.REJECTED);
            }
        }

        reimbursement.setUpdatedAt(now);
        Reimbursement saved = reimbursementRepository.save(reimbursement);
        return convertToDto(saved);
    }

    @Override
    @Transactional
    public ReimbursementDocumentDTO uploadDocument(Long reimbursementId, MultipartFile file, String documentCategory, UserDetails userDetails) throws IOException {

        Reimbursement reimbursement = getOwnedReimbursement(reimbursementId, userDetails);

        validateFile(file);

        StoredFile stored = fileStorageService.storeFile(reimbursementId, file);

        ReimbursementDocument document = new ReimbursementDocument();
        document.setReimbursementId(reimbursementId);
        document.setDocumentName(stored.originalName());
        document.setDocumentPath(stored.path());
        document.setDocumentUrl(baseUrl + "/api/reimbursements/documents/" + reimbursementId + "/" + stored.storedName());
        document.setDocumentType(stored.contentType());
        document.setDocumentSize(stored.size());
        document.setDocumentCategory(documentCategory);
        document.setIsVerified(false);
        document.setUploadedAt(LocalDateTime.now());

        ReimbursementDocument saved = documentRepository.save(document);

        log.info("Document uploaded: reimbursementId={}, documentId={}", reimbursementId, saved.getDocumentId());

        return reimbursementMapper.toDocumentDTO(saved);
    }

    private Reimbursement getOwnedReimbursement(Long reimbursementId, UserDetails userDetails) {
        Reimbursement reimbursement = reimbursementRepository.findById(reimbursementId)
                .orElseThrow(() -> new IllegalArgumentException("Reimbursement not found"));

        if (!authorizationService.isOwner(reimbursement, userDetails)) {
            throw new SecurityException("You can only upload documents to your own reimbursements");
        }
        return reimbursement;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Invalid file type. Allowed: JPEG, PNG, GIF, WEBP, PDF");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 10MB limit");
        }
    }

    @Override
    public boolean canAccessReimbursement(Long reimbursementId, UserDetails userDetails) {

        // Fast-fail guards
        if (reimbursementId == null || userDetails == null) {
            return false;
        }

        Long userIdLong = userDetails.getUser_id();
        if (userIdLong == null) {
            return false;
        }

        Integer currentUserId = userIdLong.intValue();

        Reimbursement reimbursement = reimbursementRepository.findById(reimbursementId).orElse(null);

        if (reimbursement == null) {
            return false;
        }

        return authorizationService.canAccess(reimbursement, userDetails);
    }

    @Override
    @Transactional
    public void deleteDocument(Long documentId, UserDetails userDetails) {

        if (documentId == null || userDetails == null) {
            throw new IllegalArgumentException("Invalid request");
        }

        ReimbursementDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        Long reimbursementId = document.getReimbursementId();

        Reimbursement reimbursement = reimbursementRepository.findById(reimbursementId)
                .orElseThrow(() -> new IllegalArgumentException("Reimbursement not found"));

        // Authorization (owner / approver / HR / admin)
        if (!canAccessReimbursement(reimbursementId, userDetails)) {
            throw new SecurityException("You are not authorized to delete this document");
        }

        // Business rule: prevent deletion after finalization
        if (reimbursement.getStatus() == ReimbursementStatus.APPROVED || reimbursement.getStatus() == ReimbursementStatus.PAID) {
            throw new IllegalArgumentException("Documents cannot be deleted after reimbursement is approved or paid");
        }

        // Delete file from filesystem (best effort)
        try {
            if (document.getDocumentPath() != null) {
                Path filePath = Paths.get(document.getDocumentPath());
                Files.deleteIfExists(filePath);
            }
        } catch (IOException e) {
            log.error("Failed to delete file from filesystem: documentId={}, path={}", documentId, document.getDocumentPath(), e);
            throw new RuntimeException("Failed to delete document file from filesystem");
        }

        // Delete DB record
        documentRepository.delete(document);

        log.info("Document deleted: documentId={}, reimbursementId={}, deletedBy={}", documentId, reimbursementId, userDetails.getUser_id());
    }

    @Override
    @Transactional
    public ReimbursementDTO hrApproveOrReject(ReimbursementApprovalDTO approval, UserDetails userDetails) {

        if (approval == null || userDetails == null) {
            throw new IllegalArgumentException("Invalid request");
        }

        Reimbursement reimbursement = reimbursementRepository.findById(approval.getReimbursementId()).orElseThrow(() -> new RuntimeException("Reimbursement not found"));

        // Status gate
        if (reimbursement.getStatus() != ReimbursementStatus.PENDING_HR) {
            throw new IllegalArgumentException("Reimbursement is not pending HR approval");
        }

        // Authorization: HR / Admin / Superadmin only
        if (!authorizationService.canApproveAsHr(reimbursement, userDetails)) {
            throw new SecurityException("Only HR can approve or reject reimbursements");
        }

        String action = approval.getAction();
        if (action == null) {
            throw new IllegalArgumentException("Action is required");
        }

        action = action.trim().toUpperCase();
        LocalDateTime now = LocalDateTime.now();
        Integer hrUserId = userDetails.getUser_id().intValue();

        reimbursement.setHrApproverId(hrUserId);
        reimbursement.setHrApprovedDate(now);
        reimbursement.setUpdatedAt(now);

        switch (action) {

            case "APPROVE":
                reimbursement.setStatus(ReimbursementStatus.APPROVED);
                reimbursement.setHrComment(approval.getComment());

                notificationService.send(reimbursement, null, null, ReimbursementNotificationType.APPROVED);
                break;

            case "REJECT":
                reimbursement.setStatus(ReimbursementStatus.REJECTED);
                reimbursement.setRejectionReason(approval.getRejectionReason());

                notificationService.send(reimbursement, null, null, ReimbursementNotificationType.REJECTED);
                break;

            default:
                throw new IllegalArgumentException("Invalid action. Allowed values: APPROVE, REJECT");
        }

        Reimbursement saved = reimbursementRepository.save(reimbursement);

        log.info("HR {} reimbursementId={}, hrUserId={}", action, saved.getReimbursementId(), hrUserId);

        return convertToDto(saved);
    }


    @Override
    public List<ReimbursementDTO> getPendingApprovals(UserDetails userDetails) {
        List<ReimbursementDTO> pending = new ArrayList<>();
        pending.addAll(getPendingApprovalsByType(ReimbursementStatus.PENDING_MANAGER, userDetails));
        pending.addAll(getPendingApprovalsByType(ReimbursementStatus.PENDING_HR, userDetails));
        return pending;
    }

    @Override
    public List<ReimbursementDTO> getPendingManagerApprovals(UserDetails userDetails) {
        return getPendingApprovalsByType(ReimbursementStatus.PENDING_MANAGER, userDetails);
    }

    @Override
    public List<ReimbursementDTO> getPendingHrApprovals() {
        return reimbursementRepository.findByStatus(ReimbursementStatus.PENDING_HR).stream().map(this::convertToDto).toList();
    }

    private List<ReimbursementDTO> getPendingApprovalsByType(ReimbursementStatus status, UserDetails userDetails) {
        if (userDetails == null || userDetails.getUser_id() == null) return Collections.emptyList();
        Integer userId = userDetails.getUser_id().intValue();

        if (status == ReimbursementStatus.PENDING_MANAGER) {
            return reimbursementRepository.findByApproverIdAndStatus(userId, status).stream().map(this::convertToDto).toList();
        }

        if (status == ReimbursementStatus.PENDING_HR) {
            return reimbursementRepository.findByStatus(status).stream()
                    .filter(r -> authorizationService.canApproveAsHr(r, userDetails))
                    .map(this::convertToDto).toList();
        }

        return Collections.emptyList();
    }
}
