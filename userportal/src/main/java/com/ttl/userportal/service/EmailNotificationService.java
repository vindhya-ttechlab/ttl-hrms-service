package com.ttl.userportal.service;

import com.ttl.userportal.dto.EmailDetailsDTO;
import com.ttl.userportal.entity.EmailDetails;
import com.ttl.userportal.repository.EmailDetailsRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

public class EmailNotificationService
{
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailDetailsRepository emailDetailsRepository;


    public void sendEmail(EmailDetailsDTO email) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(email.getSenderEmail(), email.getSenderEmail());
            helper.setTo(email.getReceiverEmail());
            helper.setSubject(email.getSubject());
            helper.setText(email.getMessage(), false); // false = plain text

            mailSender.send(mimeMessage);
            System.out.println(" Email sent successfully from "
                    + email.getSenderEmail() + " to " + email.getReceiverEmail());

            EmailDetails emailDetails=new EmailDetails();
            emailDetails.setSubject(email.getSubject());
            emailDetails.setReceiverEmail(email.getReceiverEmail());
            emailDetails.setSenderEmail(email.getSenderEmail());
            emailDetails.setSubject(email.getSubject());
            emailDetails.setMessage(email.getMessage());
            emailDetails.setSentDateTime(email.getSentDateTime());

            emailDetailsRepository.save(emailDetails);
//            return "message sent successfully";
        } catch (Exception e) {
            System.out.println(" Failed to send email to " + email.getReceiverEmail());
            e.printStackTrace();
//            return "message failure";
        }
    }


}
