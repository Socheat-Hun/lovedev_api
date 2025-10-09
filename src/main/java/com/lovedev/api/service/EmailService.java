package com.lovedev.api.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.from-name}")
    private String fromName;

    @Async
    public void sendVerificationEmail(String to, String token, String userName) {
        String subject = "Verify Your Email - LoveDev";
        String verificationUrl = "http://localhost:8081/api/v1/auth/verify-email?token=" + token;

        String htmlContent = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .button { display: inline-block; padding: 12px 30px; background-color: #4CAF50; 
                             color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Welcome to LoveDev!</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p>Thank you for registering with LoveDev. Please verify your email address to activate your account.</p>
                        <p>Click the button below to verify your email:</p>
                        <a href="%s" class="button">Verify Email</a>
                        <p>Or copy and paste this link into your browser:</p>
                        <p style="word-break: break-all;">%s</p>
                        <p>This link will expire in 24 hours.</p>
                        <p>If you didn't create an account, please ignore this email.</p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2024 LoveDev. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, userName, verificationUrl, verificationUrl);

        sendHtmlEmail(to, subject, htmlContent);
    }

    @Async
    public void sendPasswordResetEmail(String to, String token, String userName) {
        String subject = "Reset Your Password - LoveDev";
        String resetUrl = "http://localhost:3000/reset-password?token=" + token;

        String htmlContent = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #FF5722; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .button { display: inline-block; padding: 12px 30px; background-color: #FF5722; 
                             color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Password Reset Request</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p>We received a request to reset your password. Click the button below to create a new password:</p>
                        <a href="%s" class="button">Reset Password</a>
                        <p>Or copy and paste this link into your browser:</p>
                        <p style="word-break: break-all;">%s</p>
                        <p>This link will expire in 1 hour.</p>
                        <p>If you didn't request a password reset, please ignore this email or contact support if you have concerns.</p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2024 LoveDev. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, userName, resetUrl, resetUrl);

        sendHtmlEmail(to, subject, htmlContent);
    }

    @Async
    public void sendWelcomeEmail(String to, String userName) {
        String subject = "Welcome to LoveDev!";

        String htmlContent = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #2196F3; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Welcome Aboard!</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p>Your email has been successfully verified! Welcome to LoveDev.</p>
                        <p>You can now enjoy all the features of our platform:</p>
                        <ul>
                            <li>Access your personalized dashboard</li>
                            <li>Update your profile information</li>
                            <li>Connect with other developers</li>
                            <li>And much more!</li>
                        </ul>
                        <p>If you have any questions, feel free to reach out to our support team.</p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2024 LoveDev. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, userName);

        sendHtmlEmail(to, subject, htmlContent);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }
}