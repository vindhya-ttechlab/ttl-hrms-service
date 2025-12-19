package com.ttl.userportal.service;

import com.ttl.userportal.dto.EmailDetailsDTO;
import com.ttl.userportal.entity.EmailDetails;
import com.ttl.userportal.repository.EmailDetailsRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
public class EmailNotificationService {
    
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailDetailsRepository emailDetailsRepository;

    @Autowired
    private EmailTemplateService emailTemplateService;

    @Value("${spring.mail.username}")
    private String systemEmailAddress;

    @Value("${app.company.name:Trident Tech Lab}")
    private String companyName;

    @Value("${app.login.url:http://localhost:3000}")
    private String loginUrl;

    /**
     * Send a plain text email
     */
    public void sendEmail(EmailDetailsDTO email) {
        sendEmail(email, false);
    }

    /**
     * Send an email with option for HTML content
     * @param email Email details
     * @param isHtml Whether the message is HTML
     */
    public void sendEmail(EmailDetailsDTO email, boolean isHtml) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            String senderEmail = email.getSenderEmail() != null ? email.getSenderEmail() : systemEmailAddress;
            helper.setFrom(senderEmail);
            helper.setTo(email.getReceiverEmail());
            helper.setSubject(email.getSubject());
            helper.setText(email.getMessage(), isHtml);

            mailSender.send(mimeMessage);
            log.info("Email sent successfully from {} to {}", senderEmail, email.getReceiverEmail());

            // Save email record
            EmailDetails emailDetails = new EmailDetails();
            emailDetails.setSubject(email.getSubject());
            emailDetails.setReceiverEmail(email.getReceiverEmail());
            emailDetails.setSenderEmail(senderEmail);
            emailDetails.setMessage(email.getMessage());
            emailDetails.setSentDateTime(email.getSentDateTime() != null ? email.getSentDateTime() : LocalDateTime.now());

            emailDetailsRepository.save(emailDetails);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", email.getReceiverEmail(), e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Send email using a template code from the database.
     * Template code is fetched from email_templates table dynamically.
     * 
     * @param templateCode The template code (e.g., "NEW_USER_WELCOME", "LEAVE_REQUEST")
     * @param recipientEmail Recipient email address
     * @param sourceData Map of source data that matches placeholder sourceField definitions in the template
     */
    @Async
    public void sendTemplatedEmail(String templateCode, String recipientEmail, Map<String, Object> sourceData) {
        sendTemplatedEmailSync(templateCode, recipientEmail, null, sourceData);
    }

    /**
     * Send email using a template code (synchronous version for internal use)
     */
    public void sendTemplatedEmailSync(String templateCode, String recipientEmail, String senderEmail, 
                                        Map<String, Object> sourceData) {
        try {
            // Add common data that's always available
            sourceData.putIfAbsent("company_name", companyName);
            sourceData.putIfAbsent("login_url", loginUrl);
            
            String subject = emailTemplateService.getProcessedSubject(templateCode, sourceData);
            String body = emailTemplateService.getProcessedBody(templateCode, sourceData);
            boolean isHtml = emailTemplateService.isHtmlTemplate(templateCode);

            EmailDetailsDTO emailDTO = new EmailDetailsDTO();
            emailDTO.setReceiverEmail(recipientEmail);
            emailDTO.setSenderEmail(senderEmail != null ? senderEmail : systemEmailAddress);
            emailDTO.setSubject(subject);
            emailDTO.setMessage(body);
            emailDTO.setSentDateTime(LocalDateTime.now());

            sendEmail(emailDTO, isHtml);
            log.info("Templated email [{}] sent successfully to {}", templateCode, recipientEmail);
        } catch (Exception e) {
            log.error("Failed to send templated email [{}] to {}: {}", templateCode, recipientEmail, e.getMessage());
            e.printStackTrace();
        }
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getLoginUrl() {
        return loginUrl;
    }
}
