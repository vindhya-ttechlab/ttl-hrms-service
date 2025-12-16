package com.ttl.userportal.service;

import com.ttl.userportal.dto.*;
import com.ttl.userportal.entity.*;
import com.ttl.userportal.repository.*;
import com.ttl.userportal.util.model.UserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.ttl.userportal.entity.Role;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReimbursementService {

    @Autowired
    private ReimbursementRepository reimbursementRepository;

    @Autowired
    private ReimbursementDocumentRepository documentRepository;

    @Autowired
    private ReimbursementCategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailNotificationService emailNotificationService;

    @Value("${app.upload.dir:uploads/reimbursements}")
    private String uploadDir;

    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;

    // Allowed file types for documents
    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    // ==================== CATEGORY OPERATIONS ====================

    /**
     * Get all active reimbursement categories
     */
    public List<ReimbursementCategoryDTO> getAllCategories() {
        return categoryRepository.findByIsActiveTrueOrderByCategoryNameAsc()
                .stream()
                .map(this::convertCategoryToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get category by ID
     */
    public ReimbursementCategoryDTO getCategoryById(Integer categoryId) {
        ReimbursementCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + categoryId));
        return convertCategoryToDTO(category);
    }

    /**
     * Create a new category (Admin only)
     */
    @Transactional
    public ReimbursementCategoryDTO createCategory(ReimbursementCategoryDTO dto) {
        if (categoryRepository.existsByCategoryName(dto.getCategoryName())) {
            throw new RuntimeException("Category already exists: " + dto.getCategoryName());
        }

        ReimbursementCategory category = new ReimbursementCategory();
        category.setCategoryName(dto.getCategoryName());
        category.setDescription(dto.getDescription());
        category.setMaxAmount(dto.getMaxAmount());
        category.setRequiresReceipt(dto.getRequiresReceipt() != null ? dto.getRequiresReceipt() : true);
        category.setRequiresApproval(dto.getRequiresApproval() != null ? dto.getRequiresApproval() : true);
        category.setIsActive(true);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        ReimbursementCategory saved = categoryRepository.save(category);
        log.info("Created reimbursement category: {}", saved.getCategoryName());
        return convertCategoryToDTO(saved);
    }

    // ==================== REIMBURSEMENT OPERATIONS ====================

    /**
     * Create or update a reimbursement request
     */
    @Transactional
    public ReimbursementDTO createOrUpdateReimbursement(ReimbursementRequestDTO request, UserDetails userDetails) {
        Integer userId = userDetails.getUser_id().intValue();
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get manager as approver
        Integer managerId = user.getManager();
        if (managerId == null) {
            throw new RuntimeException("No manager assigned. Cannot submit reimbursement.");
        }

        // Validate category
        ReimbursementCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // Validate amount against max limit
        if (category.getMaxAmount() != null && request.getAmount().compareTo(category.getMaxAmount()) > 0) {
            throw new RuntimeException("Amount exceeds maximum limit of " + category.getMaxAmount() + " for category " + category.getCategoryName());
        }

        Reimbursement reimbursement;
        boolean isNew = request.getReimbursementId() == null;

        if (isNew) {
            // Create new reimbursement
            reimbursement = new Reimbursement();
            reimbursement.setUserId(userId);
            reimbursement.setAppliedDate(LocalDateTime.now());
            reimbursement.setCreatedAt(LocalDateTime.now());
        } else {
            // Update existing
            reimbursement = reimbursementRepository.findById(request.getReimbursementId())
                    .orElseThrow(() -> new RuntimeException("Reimbursement not found"));

            // Only allow update if in DRAFT or PENDING status
            String status = reimbursement.getStatus();
            if (!"DRAFT".equalsIgnoreCase(status) && !"PENDING".equalsIgnoreCase(status)) {
                throw new RuntimeException("Cannot update reimbursement in " + status + " status");
            }
        }

        // Set fields
        reimbursement.setCategoryId(request.getCategoryId());
        reimbursement.setTitle(request.getTitle());
        reimbursement.setDescription(request.getDescription());
        reimbursement.setAmount(request.getAmount());
        reimbursement.setCurrency(request.getCurrency() != null ? request.getCurrency() : "INR");
        reimbursement.setExpenseDate(request.getExpenseDate());
        reimbursement.setMerchantName(request.getMerchantName());
        reimbursement.setPaymentMethod(request.getPaymentMethod());
        reimbursement.setApproverId(managerId);
        reimbursement.setIsActive(true);
        reimbursement.setUpdatedAt(LocalDateTime.now());

        // Determine approver: prefer request.managerId (frontend) else user's assigned manager
        if (request.getManagerId() != null) {
            managerId = request.getManagerId();
        }


        // Set status based on submitForApproval flag
        if (Boolean.TRUE.equals(request.getSubmitForApproval())) {
            reimbursement.setStatus(Reimbursement.ReimbursementStatus.PENDING_MANAGER.name());

            // ensure an approver exists
            if (managerId == null) {
                throw new RuntimeException("No manager assigned. Cannot submit reimbursement.");
            }

            reimbursement.setApproverId(managerId);

            // Send email notification to manager
            sendReimbursementNotification(reimbursement, user, managerId, "NEW_REQUEST");
        } else {
            reimbursement.setStatus("DRAFT");
        }

        Reimbursement saved = reimbursementRepository.save(reimbursement);
        log.info("Saved reimbursement ID: {} for user: {}", saved.getReimbursementId(), user.getEmail());

        return convertToDTO(saved);
    }

    /**
     * Upload document for a reimbursement
     */
    @Transactional
    public ReimbursementDocumentDTO uploadDocument(Long reimbursementId, MultipartFile file,
                                                    String documentCategory, UserDetails userDetails) throws IOException {
        // Validate reimbursement exists and belongs to user
        Reimbursement reimbursement = reimbursementRepository.findById(reimbursementId)
                .orElseThrow(() -> new RuntimeException("Reimbursement not found"));

        if (!reimbursement.getUserId().equals(userDetails.getUser_id().intValue())) {
            throw new RuntimeException("You can only upload documents to your own reimbursements");
        }

        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Invalid file type. Allowed: JPEG, PNG, GIF, WEBP, PDF");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 10MB limit");
        }

        // Create upload directory
        Path uploadPath = Paths.get(uploadDir, reimbursementId.toString());
        Files.createDirectories(uploadPath);

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : getExtensionFromContentType(contentType);
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        // Save file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Create document record
        ReimbursementDocument document = new ReimbursementDocument();
        document.setReimbursementId(reimbursementId);
        document.setDocumentName(originalFilename != null ? originalFilename : uniqueFilename);
        document.setDocumentPath(filePath.toString());
        document.setDocumentUrl(baseUrl + "/api/reimbursements/documents/" + reimbursementId + "/" + uniqueFilename);
        document.setDocumentType(contentType);
        document.setDocumentSize(file.getSize());
        document.setDocumentCategory(parseDocumentCategory(documentCategory));
        document.setIsVerified(false);
        document.setUploadedAt(LocalDateTime.now());

        ReimbursementDocument saved = documentRepository.save(document);
        log.info("Uploaded document {} for reimbursement {}", saved.getDocumentName(), reimbursementId);

        return convertDocumentToDTO(saved);
    }

    /**
     * Get all reimbursements for current user
     */
    public List<ReimbursementDTO> getMyReimbursements(UserDetails userDetails) {
        Integer userId = userDetails.getUser_id().intValue();
        return reimbursementRepository.findByUserIdAndIsActiveTrueOrderByAppliedDateDesc(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get reimbursements by status for current user
     */
    public List<ReimbursementDTO> getMyReimbursementsByStatus(String status, UserDetails userDetails) {
        Integer userId = userDetails.getUser_id().intValue();
        Reimbursement.ReimbursementStatus reimbursementStatus = Reimbursement.ReimbursementStatus.valueOf(status.toUpperCase());
        return reimbursementRepository.findByUserIdAndStatusAndIsActiveTrueOrderByAppliedDateDesc(userId, reimbursementStatus)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get pending reimbursements for approval (for managers)
     */
    public List<ReimbursementDTO> getPendingApprovals(UserDetails userDetails) {
        Integer approverId = userDetails.getUser_id().intValue();
        return reimbursementRepository.findPendingApprovalsByApproverId(approverId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ReimbursementDTO> getPendingManagerApprovals(UserDetails user) {
        return reimbursementRepository
                .findByApproverIdAndStatus(
                        user.getUser_id().intValue(),
                        Reimbursement.ReimbursementStatus.PENDING_MANAGER.name()
                )
                .stream().map(this::convertToDTO).toList();
    }

    public List<ReimbursementDTO> getPendingHrApprovals() {
        return reimbursementRepository
                .findByStatus(Reimbursement.ReimbursementStatus.PENDING_HR.name())
                .stream().map(this::convertToDTO).toList();
    }


    /**
     * Get reimbursement by ID with documents
     */
    public ReimbursementDTO getReimbursementById(Long reimbursementId, UserDetails userDetails) {
        Reimbursement reimbursement = reimbursementRepository.findById(reimbursementId)
                .orElseThrow(() -> new RuntimeException("Reimbursement not found"));

        Integer userId = userDetails.getUser_id().intValue();
        // Allow access to owner or approver
        if (!reimbursement.getUserId().equals(userId) && !reimbursement.getApproverId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        return convertToDTO(reimbursement);
    }

    /**
     * Approve or reject a reimbursement
     */
    @Transactional
    public ReimbursementDTO approveOrReject(
            ReimbursementApprovalDTO approval,
            UserDetails userDetails) {

        Integer approverId = userDetails.getUser_id().intValue();

        Reimbursement reimbursement = reimbursementRepository
                .findById(approval.getReimbursementId())
                .orElseThrow(() -> new RuntimeException("Reimbursement not found"));

        // 🔒 Manager ownership check
        if (!reimbursement.getApproverId().equals(approverId)) {
            throw new RuntimeException("Only assigned manager can approve");
        }

        // 🔒 Must be waiting for manager
        if (!Reimbursement.ReimbursementStatus.PENDING_MANAGER.name()
                .equals(reimbursement.getStatus())) {
            throw new RuntimeException("Reimbursement not pending manager approval");
        }

        String action = approval.getAction().toUpperCase();
        LocalDateTime now = LocalDateTime.now();

        if ("APPROVE".equals(action)) {

            // 👉 Manager approval moves to HR queue
            reimbursement.setStatus(
                    Reimbursement.ReimbursementStatus.PENDING_HR.name()
            );
            reimbursement.setApprovedAmount(
                    approval.getApprovedAmount() != null
                            ? approval.getApprovedAmount()
                            : reimbursement.getAmount()
            );
            reimbursement.setApprovedDate(now);
            reimbursement.setApproverComment(approval.getComment());

            sendReimbursementNotification(reimbursement, null, null, "PENDING_HR");

        } else if ("REJECT".equals(action)) {

            reimbursement.setStatus(
                    Reimbursement.ReimbursementStatus.REJECTED.name()
            );
            reimbursement.setApprovedDate(now);
            reimbursement.setApproverComment(approval.getComment());
            reimbursement.setRejectionReason(approval.getRejectionReason());

            sendReimbursementNotification(reimbursement, null, null, "REJECTED");

        } else {
            throw new RuntimeException("Invalid action. Use APPROVE or REJECT");
        }

        reimbursement.setUpdatedAt(now);
        return convertToDTO(reimbursementRepository.save(reimbursement));
    }


    /**
     * Cancel a reimbursement (by employee)
     */
    @Transactional
    public ReimbursementDTO cancelReimbursement(Long reimbursementId, UserDetails userDetails) {
        Integer userId = userDetails.getUser_id().intValue();

        Reimbursement reimbursement = reimbursementRepository.findById(reimbursementId)
                .orElseThrow(() -> new RuntimeException("Reimbursement not found"));

        if (!reimbursement.getUserId().equals(userId)) {
            throw new RuntimeException("You can only cancel your own reimbursements");
        }

        if (!"DRAFT".equals(reimbursement.getStatus())
                && !Reimbursement.ReimbursementStatus.PENDING_MANAGER.name()
                .equals(reimbursement.getStatus())) {
            throw new RuntimeException("Cannot cancel reimbursement in " + reimbursement.getStatus());
        }


        reimbursement.setStatus("CANCELLED");
        reimbursement.setUpdatedAt(LocalDateTime.now());

        Reimbursement saved = reimbursementRepository.save(reimbursement);
        log.info("Cancelled reimbursement ID: {} by user: {}", reimbursementId, userId);
        return convertToDTO(saved);
    }

    /**
     * Delete a document
     */
    @Transactional
    public void deleteDocument(Long documentId, UserDetails userDetails) throws IOException {
        ReimbursementDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        Reimbursement reimbursement = reimbursementRepository.findById(document.getReimbursementId())
                .orElseThrow(() -> new RuntimeException("Reimbursement not found"));

        if (!reimbursement.getUserId().equals(userDetails.getUser_id().intValue())) {
            throw new RuntimeException("You can only delete documents from your own reimbursements");
        }

        // Delete file from filesystem
        Path filePath = Paths.get(document.getDocumentPath());
        Files.deleteIfExists(filePath);

        // Delete from database
        documentRepository.deleteById(documentId);
        log.info("Deleted document ID: {} from reimbursement: {}", documentId, document.getReimbursementId());
    }

    /**
     * Get reimbursement summary for a user
     */
    public ReimbursementSummaryDTO getReimbursementSummary(UserDetails userDetails) {
        Integer userId = userDetails.getUser_id().intValue();
        int currentYear = LocalDate.now().getYear();

        List<Reimbursement> allReimbursements = reimbursementRepository.findByUserIdAndIsActiveTrueOrderByAppliedDateDesc(userId);

        long pending = allReimbursements.stream()
                .filter(r ->
                        Reimbursement.ReimbursementStatus.PENDING_MANAGER.name().equals(r.getStatus())
                                || Reimbursement.ReimbursementStatus.PENDING_HR.name().equals(r.getStatus())
                )
                .count();

        long approved = allReimbursements.stream()
                .filter(r -> "APPROVED".equals(r.getStatus()))
                .count();

        long rejected = allReimbursements.stream()
                .filter(r -> "REJECTED".equals(r.getStatus()))
                .count();

        BigDecimal totalApproved = reimbursementRepository.sumApprovedAmountByUserAndYear(userId, currentYear);
        BigDecimal pendingAmount = allReimbursements.stream()
                .filter(r -> r.getStatus() == "PENDING")
                .map(Reimbursement::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ReimbursementSummaryDTO(pending, approved, rejected, totalApproved, pendingAmount, currentYear);
    }

    // ==================== HELPER METHODS ====================

    private void sendReimbursementNotification(Reimbursement reimbursement, Users user, Integer managerId, String type) {
        try {
            if (user == null) {
                user = userRepository.findById(reimbursement.getUserId())
                        .orElse(null);
            }
            if (user == null) return;

            EmailDetailsDTO email = new EmailDetailsDTO();
            email.setSentDateTime(LocalDateTime.now());

            switch (type) {
                case "NEW_REQUEST":
                    Users manager = userRepository.findById(managerId).orElse(null);
                    if (manager == null) return;
                    email.setReceiverEmail(manager.getEmail());
                    email.setSenderEmail(user.getEmail());
                    email.setSubject("New Reimbursement Request: " + reimbursement.getTitle());
                    email.setMessage("You have a new reimbursement request from " + user.getName() +
                            " for amount " + reimbursement.getCurrency() + " " + reimbursement.getAmount() +
                            ". Please review and approve/reject.");
                    break;

                case "APPROVED":
                    Users approver = userRepository.findById(reimbursement.getApproverId()).orElse(null);
                    email.setReceiverEmail(user.getEmail());
                    email.setSenderEmail(approver != null ? approver.getEmail() : "hr@company.com");
                    email.setSubject("Reimbursement Approved: " + reimbursement.getTitle());
                    email.setMessage("Your reimbursement request has been approved for amount " +
                            reimbursement.getCurrency() + " " + reimbursement.getApprovedAmount());
                    break;

                case "REJECTED":
                    approver = userRepository.findById(reimbursement.getApproverId()).orElse(null);
                    email.setReceiverEmail(user.getEmail());
                    email.setSenderEmail(approver != null ? approver.getEmail() : "hr@company.com");
                    email.setSubject("Reimbursement Rejected: " + reimbursement.getTitle());
                    email.setMessage("Your reimbursement request has been rejected. Reason: " +
                            reimbursement.getRejectionReason());
                    break;
            }

            // Send email asynchronously to avoid blocking request thread
            java.util.concurrent.CompletableFuture.runAsync(() -> {
                try {
                    emailNotificationService.sendEmail(email);
                } catch (Exception ex) {
                    log.error("Failed to send reimbursement notification async", ex);
                }
            });
        } catch (Exception e) {
            log.error("Failed to prepare reimbursement notification", e);
        }
    }

    public boolean canAccessReimbursement(Long reimbursementId, com.ttl.userportal.util.model.UserDetails userDetails) {
        if (userDetails == null) return false;
        Reimbursement r = reimbursementRepository.findById(reimbursementId).orElse(null);
        if (r == null) return false;

        Integer currentUserId = userDetails.getUser_id() != null ? userDetails.getUser_id().intValue() : null;
        if (currentUserId == null) return false;

        // Owner
        if (r.getUserId() != null && r.getUserId().equals(currentUserId)) return true;
        // Manager / Approver
        if (r.getApproverId() != null && r.getApproverId().equals(currentUserId)) return true;
        // HR approver
        if (r.getHrApproverId() != null && r.getHrApproverId().equals(currentUserId)) return true;

        // Roles (HR, Admin, Superadmin)
        if (userDetails.getRole() != null) {
            for (Role role : userDetails.getRole()) {
                String rn = role.getRoleName();
                if (rn == null) continue;
                String rnLower = rn.trim().toLowerCase();
                if (rnLower.equals("hr") || rnLower.equals("admin") || rnLower.equals("superadmin") || rnLower.equals("super_admin")) {
                    return true;
                }
            }
        }

        return false;
    }

    private ReimbursementDTO convertToDTO(Reimbursement r) {
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
        dto.setStatus(r.getStatus());
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

        // Fetch user name
        userRepository.findById(r.getUserId()).ifPresent(user -> {
            dto.setUserName(user.getName());
            dto.setUserEmail(user.getEmail());
        });

        // Fetch category name
        categoryRepository.findById(r.getCategoryId()).ifPresent(cat -> {
            dto.setCategoryName(cat.getCategoryName());
        });

        // Fetch approver name
        if (r.getApproverId() != null) {
            userRepository.findById(r.getApproverId()).ifPresent(approver -> {
                dto.setApproverName(approver.getName());
            });
        }

        // Fetch HR approver name
        if (r.getHrApproverId() != null) {
            userRepository.findById(r.getHrApproverId()).ifPresent(hr -> {
                dto.setHrApproverId(hr.getId());
                dto.setHrApproverName(hr.getName());
                dto.setHrApprovedDate(r.getHrApprovedDate());
                dto.setHrComment(r.getHrComment());
            });
        }

        // Fetch documents
        List<ReimbursementDocument> docs = documentRepository.findByReimbursementIdOrderByUploadedAtDesc(r.getReimbursementId());
        dto.setDocuments(docs.stream().map(this::convertDocumentToDTO).collect(Collectors.toList()));

        return dto;
    }

    private ReimbursementCategoryDTO convertCategoryToDTO(ReimbursementCategory c) {
        return new ReimbursementCategoryDTO(
                c.getCategoryId(),
                c.getCategoryName(),
                c.getDescription(),
                c.getMaxAmount(),
                c.getRequiresReceipt(),
                c.getRequiresApproval(),
                c.getIsActive(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }

    private String parseDocumentCategory(String category) {
        if (category == null || category.isEmpty()) {
            return "RECEIPT"; // default category as string
        }
        return category.toUpperCase(); // keep it uppercase if needed
    }


    private String getExtensionFromContentType(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            case "application/pdf" -> ".pdf";
            default -> ".bin";
        };
    }

    private ReimbursementDocumentDTO convertDocumentToDTO(ReimbursementDocument d) {
        return new ReimbursementDocumentDTO(
                d.getDocumentId(),
                d.getReimbursementId(),
                d.getDocumentName(),
                d.getDocumentUrl(),
                d.getDocumentType(),
                d.getDocumentSize(),
                d.getDocumentCategory() != null ? d.getDocumentCategory() : null,
                d.getIsVerified(),
                d.getVerifiedBy(),
                d.getVerifiedDate(),
                d.getUploadedAt()
        );
    }

    @Transactional
    public ReimbursementDTO hrApproveOrReject(
            ReimbursementApprovalDTO approval,
            UserDetails userDetails) {

        Reimbursement reimbursement = reimbursementRepository
                .findById(approval.getReimbursementId())
                .orElseThrow(() -> new RuntimeException("Reimbursement not found"));

        if (!Reimbursement.ReimbursementStatus.PENDING_HR.name()
                .equals(reimbursement.getStatus())) {
            throw new RuntimeException("Not pending HR approval");
        }

        String action = approval.getAction().toUpperCase();
        LocalDateTime now = LocalDateTime.now();

        if ("APPROVE".equals(action)) {
            reimbursement.setStatus(
                    Reimbursement.ReimbursementStatus.APPROVED.name()
            );
            reimbursement.setHrApproverId(
                    userDetails.getUser_id().intValue()
            );
            reimbursement.setHrApprovedDate(now);
            reimbursement.setHrComment(approval.getComment());

            sendReimbursementNotification(reimbursement, null, null, "APPROVED");

        } else if ("REJECT".equals(action)) {
            reimbursement.setStatus(
                    Reimbursement.ReimbursementStatus.REJECTED.name()
            );
            reimbursement.setHrApproverId(
                    userDetails.getUser_id().intValue()
            );
            reimbursement.setHrApprovedDate(now);
            reimbursement.setRejectionReason(
                    approval.getRejectionReason()
            );

            sendReimbursementNotification(reimbursement, null, null, "REJECTED");

        } else {
            throw new RuntimeException("Invalid action");
        }

        reimbursement.setUpdatedAt(now);
        return convertToDTO(reimbursementRepository.save(reimbursement));
    }

    // Inner DTO for summary
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ReimbursementSummaryDTO {
        private long pendingCount;
        private long approvedCount;
        private long rejectedCount;
        private BigDecimal totalApprovedAmount;
        private BigDecimal pendingAmount;
        private int year;
    }
}

