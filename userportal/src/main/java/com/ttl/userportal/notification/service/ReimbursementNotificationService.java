package com.ttl.userportal.notification.service;

import com.ttl.userportal.dto.EmailDetailsDTO;
import com.ttl.userportal.entity.Reimbursement;
import com.ttl.userportal.entity.Users;
import com.ttl.userportal.notification.enums.ReimbursementNotificationType;
import com.ttl.userportal.repository.UserRepository;
import com.ttl.userportal.service.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Log4j2
@Service
@RequiredArgsConstructor
public class ReimbursementNotificationService {

    private final UserRepository userRepository;
    private final EmailNotificationService emailNotificationService;

    public void send(Reimbursement reimbursement, Users requester, Integer managerId, ReimbursementNotificationType type) {

        Users user = requester != null
                ? requester
                : userRepository.findById(reimbursement.getUserId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found with ID: " + reimbursement.getUserId()
                ));

        EmailDetailsDTO email = buildEmail(reimbursement, user, managerId, type);

        if (email == null) {
            log.warn("Email not sent. Unsupported notification type: {}", type);
            return;
        }

        email.setSentDateTime(LocalDateTime.now());
        asyncSendEmail(email);

        log.info("Notification [{}] queued for reimbursementId={}, userId={}", type, reimbursement.getReimbursementId(), user.getId()
        );
    }

    private EmailDetailsDTO buildEmail(Reimbursement r, Users user, Integer managerId, ReimbursementNotificationType type) {

        EmailDetailsDTO email = new EmailDetailsDTO();

        switch (type) {

            case NEW_REQUEST -> {
                Users manager = userRepository.findById(managerId).orElse(null);
                if (manager == null) return null;

                email.setReceiverEmail(manager.getEmail());
                email.setSenderEmail(user.getEmail());
                email.setSubject("New Reimbursement Request: " + r.getTitle());
                email.setMessage(
                        "You have a new reimbursement request from " + user.getName() +
                                " for amount " + r.getCurrency() + " " + r.getAmount()
                );
            }

            case APPROVED -> {
                // Determine who approved: manager or HR
                Users approver = null;
                String approverType = "Manager";
                
                if (r.getHrApproverId() != null && r.getHrApprovedDate() != null) {
                    // HR approved
                    approver = userRepository.findById(r.getHrApproverId()).orElse(null);
                    approverType = "HR";
                } else if (r.getApproverId() != null) {
                    // Manager approved
                    approver = userRepository.findById(r.getApproverId()).orElse(null);
                }
                
                email.setReceiverEmail(user.getEmail());
                email.setSenderEmail(
                        approver != null ? approver.getEmail() : "hr@company.com"
                );
                email.setSubject("Reimbursement Approved: " + r.getTitle());
                email.setMessage(
                        "Your reimbursement has been approved by " + approverType + 
                        ". Approved amount: " + r.getCurrency() + " " + 
                        (r.getApprovedAmount() != null ? r.getApprovedAmount() : r.getAmount())
                );
            }

            case REJECTED -> {
                // Determine who rejected: manager or HR
                Users rejector = null;
                String rejectorType = "Manager";
                
                if (r.getHrApproverId() != null && r.getHrApprovedDate() != null) {
                    // HR rejected
                    rejector = userRepository.findById(r.getHrApproverId()).orElse(null);
                    rejectorType = "HR";
                } else if (r.getApproverId() != null) {
                    // Manager rejected
                    rejector = userRepository.findById(r.getApproverId()).orElse(null);
                }
                
                email.setReceiverEmail(user.getEmail());
                email.setSenderEmail(
                        rejector != null ? rejector.getEmail() : "hr@company.com"
                );
                email.setSubject("Reimbursement Rejected: " + r.getTitle());
                email.setMessage(
                        "Your reimbursement has been rejected by " + rejectorType + 
                        ". Reason: " + (r.getRejectionReason() != null ? r.getRejectionReason() : "No reason provided")
                );
            }
        }

        return email;
    }

    @Async("notificationExecutor")
    void asyncSendEmail(EmailDetailsDTO email) {
        try {
            emailNotificationService.sendEmail(email);
        } catch (Exception e) {
            log.error("Email send failed to {}: {}", email.getReceiverEmail(), e.getMessage());
        }
    }
}