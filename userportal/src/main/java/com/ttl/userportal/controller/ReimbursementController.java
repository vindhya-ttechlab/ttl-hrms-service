package com.ttl.userportal.controller;

import com.ttl.userportal.config.CurrentUser;
import com.ttl.userportal.dto.*;
import com.ttl.userportal.service.ReimbursementService;
import com.ttl.userportal.util.model.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reimbursements")
@CrossOrigin(origins ="http://localhost:3000")
public class ReimbursementController {

    @Autowired
    private ReimbursementService reimbursementService;
    /**
     * Get all active reimbursement categories
     */
    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getAllCategories(@CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ReimbursementCategoryDTO> categories = reimbursementService.getAllCategories();
            response.put("data", categories);
            response.put("message", "Categories retrieved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("message", "Failed to retrieve categories");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Create a new category (Admin only)
     */
    @PreAuthorize("hasAuthority('REIMBURSEMENT_CATEGORY_WRITE')")
    @PostMapping("/categories")
    public ResponseEntity<Map<String, Object>> createCategory(
            @RequestBody ReimbursementCategoryDTO dto,
            @CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            ReimbursementCategoryDTO created = reimbursementService.createCategory(dto);
            response.put("data", created);
            response.put("message", "Category created successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("message", "Failed to create category");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ==================== REIMBURSEMENT ENDPOINTS ====================

    /**
     * Create or update a reimbursement request
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrUpdateReimbursement(
            @RequestBody ReimbursementRequestDTO request,
            @CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (userDetails == null) {
                response.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            ReimbursementDTO created = reimbursementService.createOrUpdateReimbursement(request, userDetails);
            response.put("data", created);
            response.put("message", "Reimbursement saved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("message", "Failed to save reimbursement");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Get all reimbursements for current user
     */
    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyReimbursements(@CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (userDetails == null) {
                response.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            List<ReimbursementDTO> reimbursements = reimbursementService.getMyReimbursements(userDetails);
            response.put("data", reimbursements);
            response.put("message", "Reimbursements retrieved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("message", "Failed to retrieve reimbursements");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get reimbursements by status for current user
     */
    @GetMapping("/my/status/{status}")
    public ResponseEntity<Map<String, Object>> getMyReimbursementsByStatus(
            @PathVariable String status,
            @CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (userDetails == null) {
                response.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            List<ReimbursementDTO> reimbursements = reimbursementService.getMyReimbursementsByStatus(status, userDetails);
            response.put("data", reimbursements);
            response.put("message", "Reimbursements retrieved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("message", "Failed to retrieve reimbursements");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get reimbursement by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getReimbursementById(
            @PathVariable Long id,
            @CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (userDetails == null) {
                response.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            ReimbursementDTO reimbursement = reimbursementService.getReimbursementById(id, userDetails);
            response.put("data", reimbursement);
            response.put("message", "Reimbursement retrieved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("message", "Failed to retrieve reimbursement");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Get reimbursement summary for current user
     */
    @GetMapping("/my/summary")
    public ResponseEntity<Map<String, Object>> getMyReimbursementSummary(@CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (userDetails == null) {
                response.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            ReimbursementService.ReimbursementSummaryDTO summary = reimbursementService.getReimbursementSummary(userDetails);
            response.put("data", summary);
            response.put("message", "Summary retrieved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("message", "Failed to retrieve summary");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Cancel a reimbursement
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelReimbursement(
            @PathVariable Long id,
            @CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (userDetails == null) {
                response.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            ReimbursementDTO cancelled = reimbursementService.cancelReimbursement(id, userDetails);
            response.put("data", cancelled);
            response.put("message", "Reimbursement cancelled successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("message", "Failed to cancel reimbursement");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ==================== APPROVAL ENDPOINTS ====================

    /**
     * Get pending approvals for manager
     */
    @GetMapping("/approvals/pending")
    public ResponseEntity<Map<String, Object>> getPendingApprovals(@CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (userDetails == null) {
                response.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            List<ReimbursementDTO> pending = reimbursementService.getPendingApprovals(userDetails);
            response.put("data", pending);
            response.put("message", "Pending approvals retrieved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("message", "Failed to retrieve pending approvals");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/approvals/manager/pending")
    public ResponseEntity<?> getManagerPending(@CurrentUser UserDetails user) {
        return ResponseEntity.ok(
                reimbursementService.getPendingManagerApprovals(user)
        );
    }

    @PreAuthorize("hasAuthority('REIMBURSEMENT_HR_APPROVE')")
    @GetMapping("/approvals/hr/pending")
    public ResponseEntity<?> getHrPending(@CurrentUser UserDetails user) {
        return ResponseEntity.ok(
                reimbursementService.getPendingHrApprovals()
        );
    }

    /**
     * Approve or reject a reimbursement
     */
    @PostMapping("/approvals/action")
    public ResponseEntity<Map<String, Object>> approveOrReject(
            @RequestBody ReimbursementApprovalDTO approval,
            @CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (userDetails == null) {
                response.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            ReimbursementDTO result = reimbursementService.approveOrReject(approval, userDetails);
            response.put("data", result);
            response.put("message", "Reimbursement " + approval.getAction().toLowerCase() + "d successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("message", "Failed to process approval action");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    // ==================== DOCUMENT ENDPOINTS ====================
    /**
     * Upload document for a reimbursement
     */
    @PostMapping("/{reimbursementId}/documents")
    public ResponseEntity<Map<String, Object>> uploadDocument(
            @PathVariable Long reimbursementId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "RECEIPT") String category,
            @CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (userDetails == null) {
                response.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            ReimbursementDocumentDTO document = reimbursementService.uploadDocument(
                    reimbursementId, file, category, userDetails);
            response.put("data", document);
            response.put("message", "Document uploaded successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            response.put("message", "Invalid file");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (IOException e) {
            response.put("error", e.getMessage());
            response.put("message", "Failed to upload document");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("message", "Failed to upload document");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Serve document file
     */
    @GetMapping("/documents/{reimbursementId}/{filename}")
    public ResponseEntity<Resource> serveDocument(
            @PathVariable Long reimbursementId,
            @PathVariable String filename,
            @CurrentUser com.ttl.userportal.util.model.UserDetails userDetails)  {
        try {
            // Require authentication
            if (userDetails == null || userDetails.getUser_id() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            // Authorization check: only owner, approver, HR, admin, or HR approver may access
            if (!reimbursementService.canAccessReimbursement(reimbursementId, userDetails)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            Path filePath = Paths.get("uploads/reimbursements", reimbursementId.toString(), filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = determineContentType(filename);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete a document
     */
    @DeleteMapping("/documents/{documentId}")
    public ResponseEntity<Map<String, Object>> deleteDocument(
            @PathVariable Long documentId,
            @CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (userDetails == null) {
                response.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            reimbursementService.deleteDocument(documentId, userDetails);
            response.put("message", "Document deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("message", "Failed to delete document");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ==================== HELPER METHODS ====================

    private String determineContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".pdf")) return "application/pdf";
        return "application/octet-stream";
    }

    @PreAuthorize("hasAuthority('REIMBURSEMENT_HR_APPROVE')")
    @PostMapping("/approvals/hr")
    public ResponseEntity<?> hrApproveOrReject(
            @RequestBody ReimbursementApprovalDTO approval,
            @CurrentUser UserDetails userDetails) {

        ReimbursementDTO dto =
                reimbursementService.hrApproveOrReject(approval, userDetails);

        return ResponseEntity.ok(dto);
    }

}

