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

    @Value("${server.app.base-url-api}")
    private String baseUrlApi;

    @Value("${server.app.base-url-web}")
    private String baseUrlWeb;

    @Async("taskExecutor")
    public void sendVerificationEmail(String to, String token, String userName) {
        String subject = "Verify Your Email - LoveDev";
        String verificationUrl = baseUrlApi+"/api/v1/auth/verify-email?token=" + token;

        String htmlContent = String.format("""
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; }
                body {
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Inter', sans-serif;
                    background: linear-gradient(135deg, #E8EAF6 0%%, #F3E5F5 100%%);
                    padding: 40px 20px;
                    line-height: 1.6;
                }
                .email-container {
                    max-width: 600px;
                    margin: 0 auto;
                    background: white;
                    border-radius: 16px;
                    overflow: hidden;
                    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
                }
                .header {
                    background: linear-gradient(135deg, #2563EB 0%%, #3B82F6 100%%);
                    padding: 60px 40px;
                    text-align: center;
                    color: white;
                }
                .header h1 {
                    font-size: 32px;
                    font-weight: 600;
                    margin-bottom: 8px;
                }
                .header p {
                    opacity: 0.9;
                    font-size: 16px;
                }
                .content {
                    padding: 50px 40px;
                }
                .greeting {
                    font-size: 20px;
                    color: #1F2937;
                    margin-bottom: 24px;
                    font-weight: 500;
                }
                .message {
                    color: #6B7280;
                    font-size: 16px;
                    line-height: 1.7;
                    margin-bottom: 32px;
                }
                .button-container {
                    text-align: center;
                    margin: 40px 0;
                }
                .button {
                    display: inline-block;
                    background: linear-gradient(135deg, #2563EB 0%%, #3B82F6 100%%);
                    color: white;
                    padding: 16px 48px;
                    text-decoration: none;
                    border-radius: 12px;
                    font-weight: 600;
                    font-size: 16px;
                    box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
                    transition: transform 0.2s;
                }
                .button:hover {
                    transform: translateY(-2px);
                }
                .link-box {
                    background: #F9FAFB;
                    border: 1px solid #E5E7EB;
                    border-radius: 8px;
                    padding: 16px;
                    margin: 24px 0;
                    word-break: break-all;
                }
                .link-box a {
                    color: #2563EB;
                    text-decoration: none;
                    font-size: 14px;
                }
                .footer {
                    background: #F9FAFB;
                    padding: 32px 40px;
                    text-align: center;
                    color: #6B7280;
                    font-size: 14px;
                }
                .footer-brand {
                    color: #1F2937;
                    font-weight: 600;
                    font-size: 16px;
                    margin-bottom: 8px;
                }
                @media only screen and (max-width: 600px) {
                    .content { padding: 40px 24px; }
                    .header { padding: 40px 24px; }
                    .header h1 { font-size: 28px; }
                }
            </style>
        </head>
        <body>
            <div class="email-container">
                <div class="header">
                    <h1>🎉 Welcome to LoveDev</h1>
                    <p>Please verify your email to get started</p>
                </div>
                
                <div class="content">
                    <div class="greeting">Hi %s,</div>
                    
                    <div class="message">
                        Thank you for creating your LoveDev account. We're excited to have you on board!
                        <br><br>
                        To complete your registration and activate your account, please verify your email address by clicking the button below.
                    </div>
                    
                    <div class="button-container">
                        <a href="%s" class="button">Verify Email Address</a>
                    </div>
                    
                    <div class="message" style="font-size: 14px; text-align: center;">
                        This link will expire in <strong>24 hours</strong>
                    </div>
                    
                    <div style="margin: 32px 0; height: 1px; background: #E5E7EB;"></div>
                    
                    <div class="message" style="font-size: 14px;">
                        If the button doesn't work, copy and paste this link into your browser:
                    </div>
                    
                    <div class="link-box">
                        <a href="%s">%s</a>
                    </div>
                    
                    <div class="message" style="font-size: 14px; color: #9CA3AF;">
                        If you didn't create this account, you can safely ignore this email.
                    </div>
                </div>
                
                <div class="footer">
                    <div class="footer-brand">LoveDev</div>
                    <div>Building amazing applications together</div>
                    <div style="margin-top: 16px;">
                        Questions? Contact us at <a href="mailto:support@lovedev.com" style="color: #2563EB;">support@lovedev.com</a>
                    </div>
                    <div style="margin-top: 24px; font-size: 12px; color: #9CA3AF;">
                        © 2024 LoveDev. All rights reserved.
                    </div>
                </div>
            </div>
        </body>
        </html>
        """, userName, verificationUrl, verificationUrl, verificationUrl);

        sendHtmlEmail(to, subject, htmlContent);
    }

    @Async("taskExecutor")
    public void sendPasswordResetEmail(String to, String token, String userName) {
        String subject = "Reset Your Password - LoveDev";
        String resetUrl = baseUrlWeb+"/reset-password?token=" + token;

        String htmlContent = String.format("""
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; }
                body {
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Inter', sans-serif;
                    background: linear-gradient(135deg, #E8EAF6 0%%, #F3E5F5 100%%);
                    padding: 40px 20px;
                    line-height: 1.6;
                }
                .email-container {
                    max-width: 600px;
                    margin: 0 auto;
                    background: white;
                    border-radius: 16px;
                    overflow: hidden;
                    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
                }
                .header {
                    background: linear-gradient(135deg, #2563EB 0%%, #3B82F6 100%%);
                    padding: 60px 40px;
                    text-align: center;
                    color: white;
                }
                .header h1 {
                    font-size: 32px;
                    font-weight: 600;
                    margin-bottom: 8px;
                }
                .header p {
                    opacity: 0.9;
                    font-size: 16px;
                }
                .content {
                    padding: 50px 40px;
                }
                .greeting {
                    font-size: 20px;
                    color: #1F2937;
                    margin-bottom: 24px;
                    font-weight: 500;
                }
                .message {
                    color: #6B7280;
                    font-size: 16px;
                    line-height: 1.7;
                    margin-bottom: 32px;
                }
                .button-container {
                    text-align: center;
                    margin: 40px 0;
                }
                .button {
                    display: inline-block;
                    background: linear-gradient(135deg, #2563EB 0%%, #3B82F6 100%%);
                    color: white;
                    padding: 16px 48px;
                    text-decoration: none;
                    border-radius: 12px;
                    font-weight: 600;
                    font-size: 16px;
                    box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
                    transition: transform 0.2s;
                }
                .button:hover {
                    transform: translateY(-2px);
                }
                .link-box {
                    background: #F9FAFB;
                    border: 1px solid #E5E7EB;
                    border-radius: 8px;
                    padding: 16px;
                    margin: 24px 0;
                    word-break: break-all;
                }
                .link-box a {
                    color: #2563EB;
                    text-decoration: none;
                    font-size: 14px;
                }
                .warning-box {
                    background: #FEF3C7;
                    border-left: 4px solid #F59E0B;
                    padding: 16px;
                    margin: 24px 0;
                    border-radius: 8px;
                }
                .warning-box p {
                    color: #92400E;
                    font-size: 14px;
                    margin: 0;
                }
                .footer {
                    background: #F9FAFB;
                    padding: 32px 40px;
                    text-align: center;
                    color: #6B7280;
                    font-size: 14px;
                }
                .footer-brand {
                    color: #1F2937;
                    font-weight: 600;
                    font-size: 16px;
                    margin-bottom: 8px;
                }
                @media only screen and (max-width: 600px) {
                    .content { padding: 40px 24px; }
                    .header { padding: 40px 24px; }
                    .header h1 { font-size: 28px; }
                }
            </style>
        </head>
        <body>
            <div class="email-container">
                <div class="header">
                    <h1>🔐 Reset Your Password</h1>
                    <p>We received a request to reset your password</p>
                </div>
                
                <div class="content">
                    <div class="greeting">Hi %s,</div>
                    
                    <div class="message">
                        We received a request to reset the password for your LoveDev account.
                        <br><br>
                        If you made this request, click the button below to create a new password:
                    </div>
                    
                    <div class="button-container">
                        <a href="%s" class="button">Reset Password</a>
                    </div>
                    
                    <div class="message" style="font-size: 14px; text-align: center;">
                        This link will expire in <strong>1 hour</strong>
                    </div>
                    
                    <div style="margin: 32px 0; height: 1px; background: #E5E7EB;"></div>
                    
                    <div class="message" style="font-size: 14px;">
                        If the button doesn't work, copy and paste this link into your browser:
                    </div>
                    
                    <div class="link-box">
                        <a href="%s">%s</a>
                    </div>
                    
                    <div class="warning-box">
                        <p>
                            <strong>⚠️ Security Notice:</strong><br>
                            If you didn't request a password reset, please ignore this email or contact support if you have concerns.
                        </p>
                    </div>
                </div>
                
                <div class="footer">
                    <div class="footer-brand">LoveDev</div>
                    <div>Building amazing applications together</div>
                    <div style="margin-top: 16px;">
                        Questions? Contact us at <a href="mailto:support@lovedev.com" style="color: #2563EB;">support@lovedev.com</a>
                    </div>
                    <div style="margin-top: 24px; font-size: 12px; color: #9CA3AF;">
                        © 2024 LoveDev. All rights reserved.
                    </div>
                </div>
            </div>
        </body>
        </html>
        """, userName, resetUrl, resetUrl, resetUrl);

        sendHtmlEmail(to, subject, htmlContent);
    }

    @Async("taskExecutor")
    public void sendWelcomeEmail(String to, String userName) {
        String subject = "Welcome to LoveDev!";

        String htmlContent = String.format("""
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; }
                body {
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Inter', sans-serif;
                    background: linear-gradient(135deg, #E8EAF6 0%%, #F3E5F5 100%%);
                    padding: 40px 20px;
                    line-height: 1.6;
                }
                .email-container {
                    max-width: 600px;
                    margin: 0 auto;
                    background: white;
                    border-radius: 16px;
                    overflow: hidden;
                    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
                }
                .header {
                    background: linear-gradient(135deg, #2563EB 0%%, #3B82F6 100%%);
                    padding: 60px 40px;
                    text-align: center;
                    color: white;
                }
                .header h1 {
                    font-size: 32px;
                    font-weight: 600;
                    margin-bottom: 8px;
                }
                .header p {
                    opacity: 0.9;
                    font-size: 16px;
                }
                .content {
                    padding: 50px 40px;
                }
                .success-badge {
                    background: linear-gradient(135deg, #10B981 0%%, #34D399 100%%);
                    color: white;
                    padding: 12px 24px;
                    border-radius: 24px;
                    display: inline-block;
                    font-weight: 600;
                    font-size: 14px;
                    margin-bottom: 32px;
                }
                .greeting {
                    font-size: 20px;
                    color: #1F2937;
                    margin-bottom: 24px;
                    font-weight: 500;
                }
                .message {
                    color: #6B7280;
                    font-size: 16px;
                    line-height: 1.7;
                    margin-bottom: 32px;
                }
                .features {
                    display: grid;
                    grid-template-columns: 1fr 1fr;
                    gap: 16px;
                    margin: 32px 0;
                }
                .feature-card {
                    background: #F9FAFB;
                    border: 1px solid #E5E7EB;
                    border-radius: 12px;
                    padding: 24px;
                    text-align: center;
                }
                .feature-card .emoji {
                    font-size: 32px;
                    margin-bottom: 12px;
                }
                .feature-card h3 {
                    color: #1F2937;
                    font-size: 16px;
                    font-weight: 600;
                    margin-bottom: 8px;
                }
                .feature-card p {
                    color: #6B7280;
                    font-size: 14px;
                    margin: 0;
                }
                .footer {
                    background: #F9FAFB;
                    padding: 32px 40px;
                    text-align: center;
                    color: #6B7280;
                    font-size: 14px;
                }
                .footer-brand {
                    color: #1F2937;
                    font-weight: 600;
                    font-size: 16px;
                    margin-bottom: 8px;
                }
                @media only screen and (max-width: 600px) {
                    .content { padding: 40px 24px; }
                    .header { padding: 40px 24px; }
                    .header h1 { font-size: 28px; }
                    .features { grid-template-columns: 1fr; }
                }
            </style>
        </head>
        <body>
            <div class="email-container">
                <div class="header">
                    <h1>🎉 Welcome to LoveDev</h1>
                    <p>Your account is now active</p>
                </div>
                
                <div class="content">
                    <div style="text-align: center;">
                        <span class="success-badge">✓ Email Verified</span>
                    </div>
                    
                    <div class="greeting">Hi %s,</div>
                    
                    <div class="message">
                        Congratulations! Your email has been successfully verified, and your LoveDev account is now fully activated.
                        <br><br>
                        We're thrilled to have you as part of our community! You now have full access to all features.
                    </div>
                    
                    <div class="features">
                        <div class="feature-card">
                            <div class="emoji">👤</div>
                            <h3>Complete Profile</h3>
                            <p>Add your info and photo</p>
                        </div>
                        
                        <div class="feature-card">
                            <div class="emoji">🚀</div>
                            <h3>Start Building</h3>
                            <p>Access all API features</p>
                        </div>
                        
                        <div class="feature-card">
                            <div class="emoji">🔐</div>
                            <h3>Secure Account</h3>
                            <p>Enable 2FA protection</p>
                        </div>
                        
                        <div class="feature-card">
                            <div class="emoji">💬</div>
                            <h3>Get Support</h3>
                            <p>We're here to help 24/7</p>
                        </div>
                    </div>
                    
                    <div style="margin: 32px 0; height: 1px; background: #E5E7EB;"></div>
                    
                    <div class="message" style="text-align: center; font-size: 14px;">
                        Need help? Check our <a href="#" style="color: #2563EB;">documentation</a> or
                        <a href="#" style="color: #2563EB;">contact support</a>
                    </div>
                </div>
                
                <div class="footer">
                    <div class="footer-brand">LoveDev</div>
                    <div>Building amazing applications together</div>
                    <div style="margin-top: 16px;">
                        Questions? Contact us at <a href="mailto:support@lovedev.com" style="color: #2563EB;">support@lovedev.com</a>
                    </div>
                    <div style="margin-top: 24px; font-size: 12px; color: #9CA3AF;">
                        © 2024 LoveDev. All rights reserved.
                    </div>
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