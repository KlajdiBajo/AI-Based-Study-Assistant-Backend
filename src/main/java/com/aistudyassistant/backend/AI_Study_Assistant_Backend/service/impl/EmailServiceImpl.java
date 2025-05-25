package com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.impl;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender javaMailSender;

    @Async
    @Retryable(
            retryFor = MessagingException.class,
            maxAttempts = 4,
            backoff = @Backoff(delay = 3000)
    )
    public CompletableFuture<Integer> sendEmailWithRetry(String to, String otp) throws MessagingException, UnsupportedEncodingException {
        try {
            sendOtpByEmail(to, otp);
            return CompletableFuture.completedFuture(1);
        } catch (MessagingException e) {
            return CompletableFuture.completedFuture(handleMessagingException(e));
        } catch (UnsupportedEncodingException e) {
            return CompletableFuture.completedFuture(handleUnsupportedEncodingException(e));
        }
    }

    @Recover
    public int handleMessagingException(MessagingException e) {
        log.error("Maximum attempt reached, failed to send email");
        log.error("Error message: {}", e.getMessage());
        return -1;
    }

    @Recover
    public int handleUnsupportedEncodingException(UnsupportedEncodingException e) {
        log.error("Maximum attempt reached , failed to send email");
        log.error("Error message : {}", e.getMessage());
        return -1;
    }

    public void sendOtpByEmail(String to, String otp) throws MessagingException, UnsupportedEncodingException {
        log.info("Trying to send email to {}", to);
        String senderName = "AIBasedStudyAssistant";
        String from = "aibasedstudyassistantplatform@gmail.com";

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(from, senderName);
        helper.setTo(to);
        helper.setSubject("One Time Password (OTP) to verify your Email Address");

        String htmlContent = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<title>Verify Your Email</title>" +
                "</head>" +
                "<body style='font-family: Arial, sans-serif; background: #f7f7fb; padding: 30px;'>" +
                "<div style='max-width: 480px; margin: auto; background: #fff; border-radius: 10px; box-shadow: 0 2px 8px #ececec; padding: 32px 40px; text-align: center;'>" +
                "  <h2 style='color: #4445d7; margin-bottom: 12px;'>Welcome to <span style='color: #4445d7;'>AI Study Assistant</span></h2>" +
                "  <p style='color:#222; margin-bottom: 28px;'>Please use the code below to verify your email address:</p>" +
                "  <div style='width:100%; text-align:center; margin-bottom: 32px;'>" +
                "    <span style='display:inline-block; font-size: 28px; font-weight: bold; color: #4445d7; background: #f7f7fb; padding: 10px 24px; border-radius: 8px; letter-spacing: 2px;'>" + otp + "</span>" +
                "  </div>" +
                "  <p style='color: #444; margin-top: 28px;'>This code is valid for <b>10 minutes</b>.</p>" +
                "  <p style='color: #ab0e3e; margin-top: 6px;'>Do not share this code with anyone.</p>" +
                "  <p style='color: #888; margin-top: 32px;'>Regards,<br/><span style='color: #4445d7;'>AI Study Assistant Team</span></p>" +
                "</div>" +
                "</body>" +
                "</html>";

        helper.setText(htmlContent, true);
        javaMailSender.send(message);
        log.info("Email has been sent successfully to {}", to);
    }
}
