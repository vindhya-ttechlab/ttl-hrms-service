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
@CrossOrigin(origins = "*")
public class ReimbursementController {

    @Autowired
    private ReimbursementService reimbursementService;

    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getAllCategories(@CurrentUser UserDetails userDetails) {
        List<ReimbursementCategoryDTO> categories = reimbursementService.getAllCategories();
        Map<String, Object> response = new HashMap<>();
        response.put("data", categories);
        response.put("message", "Categories retrieved successfully");
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('REIMBURSEMENT_CATEGORY_WRITE')")
    @PostMapping("/categories")
    public ResponseEntity<Map<String, Object>> createCategory(@RequestBody ReimbursementCategoryDTO request, @CurrentUser UserDetails userDetails) {
        ReimbursementCategoryDTO created = reimbursementService.createCategory(request, userDetails);
        Map<String, Object> response = new HashMap<>();
        response.put("data", created);
        response.put("message", "Category created successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrUpdateReimbursement(@RequestBody ReimbursementRequestDTO request, @CurrentUser UserDetails userDetails) {
        if (userDetails == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "User not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        ReimbursementDTO created = reimbursementService.createOrUpdateReimbursement(request, userDetails);
        Map<String, Object> response = new HashMap<>();
        response.put("data", created);
        response.put("message", "Reimbursement saved successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyReimbursements(@CurrentUser UserDetails userDetails) {
        if (userDetails == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "User not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        List<ReimbursementDTO> reimbursements = reimbursementService.getMyReimbursements(userDetails);
        Map<String, Object> response = new HashMap<>();
        response.put("data", reimbursements);
        response.put("message", "Reimbursements retrieved successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my/status/{status}")
    public ResponseEntity<Map<String, Object>> getMyReimbursementsByStatus(@PathVariable String status, @CurrentUser UserDetails userDetails) {
        if (userDetails == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "User not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        List<ReimbursementDTO> reimbursements = reimbursementService.getMyReimbursementsByStatus(status, userDetails);
        Map<String, Object> response = new HashMap<>();
        response.put("data", reimbursements);
        response.put("message", "Reimbursements retrieved successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getReimbursementById(@PathVariable Long id, @CurrentUser UserDetails userDetails) {
        if (userDetails == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "User not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        ReimbursementDTO reimbursement = reimbursementService.getReimbursementById(id, userDetails);
        Map<String, Object> response = new HashMap<>();
        response.put("data", reimbursement);
        response.put("message", "Reimbursement retrieved successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my/summary")
    public ResponseEntity<Map<String, Object>> getMyReimbursementSummary(@CurrentUser UserDetails userDetails) {
        if (userDetails == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "User not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        ReimbursementSummaryDTO summary = reimbursementService.getReimbursementSummary(userDetails);
        Map<String, Object> response = new HashMap<>();
        response.put("data", summary);
        response.put("message", "Summary retrieved successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelReimbursement(@PathVariable Long id, @CurrentUser UserDetails userDetails) {
        if (userDetails == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "User not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        ReimbursementDTO cancelled = reimbursementService.cancelReimbursement(id, userDetails);
        Map<String, Object> response = new HashMap<>();
        response.put("data", cancelled);
        response.put("message", "Reimbursement cancelled successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/approvals/pending")
    public ResponseEntity<Map<String, Object>> getPendingApprovals(@CurrentUser UserDetails userDetails) {
        if (userDetails == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "User not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        List<ReimbursementDTO> pending = reimbursementService.getPendingApprovals(userDetails);
        Map<String, Object> response = new HashMap<>();
        response.put("data", pending);
        response.put("message", "Pending approvals retrieved successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/approvals/manager/pending")
    public ResponseEntity<?> getManagerPending(@CurrentUser UserDetails user) {
        return ResponseEntity.ok(reimbursementService.getPendingManagerApprovals(user));
    }

    @PreAuthorize("hasAuthority('REIMBURSEMENT_HR_APPROVE')")
    @GetMapping("/approvals/hr/pending")
    public ResponseEntity<?> getHrPending(@CurrentUser UserDetails user) {
        return ResponseEntity.ok(reimbursementService.getPendingHrApprovals());
    }

    @PostMapping("/approvals/action")
    public ResponseEntity<Map<String, Object>> approveOrReject(@RequestBody ReimbursementApprovalDTO approval, @CurrentUser UserDetails userDetails) {
        if (userDetails == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "User not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        ReimbursementDTO result = reimbursementService.approveOrReject(approval, userDetails);
        Map<String, Object> response = new HashMap<>();
        response.put("data", result);
        response.put("message", "Reimbursement " + approval.getAction().toLowerCase() + "d successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{reimbursementId}/documents")
    public ResponseEntity<Map<String, Object>> uploadDocument(@PathVariable Long reimbursementId, @RequestParam("file") MultipartFile file, @RequestParam(value = "category", defaultValue = "RECEIPT") String category, @CurrentUser UserDetails userDetails) throws IOException {
        if (userDetails == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "User not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        ReimbursementDocumentDTO document = reimbursementService.uploadDocument(reimbursementId, file, category, userDetails);
        Map<String, Object> response = new HashMap<>();
        response.put("data", document);
        response.put("message", "Document uploaded successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/documents/{reimbursementId}/{filename}")
    public ResponseEntity<Resource> serveDocument(@PathVariable Long reimbursementId, @PathVariable String filename, @CurrentUser com.ttl.userportal.util.model.UserDetails userDetails) throws MalformedURLException {
        if (userDetails == null || userDetails.getUser_id() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
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
    }

    @DeleteMapping("/documents/{documentId}")
    public ResponseEntity<Map<String, Object>> deleteDocument(@PathVariable Long documentId, @CurrentUser UserDetails userDetails) {
        if (userDetails == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "User not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        reimbursementService.deleteDocument(documentId, userDetails);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Document deleted successfully");
        return ResponseEntity.ok(response);
    }

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
    public ResponseEntity<?> hrApproveOrReject(@RequestBody ReimbursementApprovalDTO approval, @CurrentUser UserDetails userDetails) {
        ReimbursementDTO dto = reimbursementService.hrApproveOrReject(approval, userDetails);
        return ResponseEntity.ok(dto);
    }
}
