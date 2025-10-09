package com.lovedev.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
@ConfigurationProperties(prefix = "app.email")
@Data
public class MailConfig {

    /**
     * From email address
     */
    private String from = "noreply@lovedev.com";

    /**
     * From name
     */
    private String fromName = "LoveDev Team";

    /**
     * Email verification URL
     */
    private String verificationUrl = "http://localhost:8081/api/v1/auth/verify-email";

    /**
     * Reset password URL
     */
    private String resetPasswordUrl = "http://localhost:3000/reset-password";

    /**
     * Email template path
     */
    private String templatePath = "classpath:/templates/";

    /**
     * Enable email sending
     */
    private boolean enabled = true;

    /**
     * SMTP host
     */
    private String host = "localhost";

    /**
     * SMTP port
     */
    private int port = 1025;

    /**
     * SMTP username
     */
    private String username;

    /**
     * SMTP password
     */
    private String password;

    /**
     * Enable SMTP authentication
     */
    private boolean auth = false;

    /**
     * Enable STARTTLS
     */
    private boolean starttls = false;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);

        if (username != null && !username.isEmpty()) {
            mailSender.setUsername(username);
        }

        if (password != null && !password.isEmpty()) {
            mailSender.setPassword(password);
        }

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", String.valueOf(auth));
        props.put("mail.smtp.starttls.enable", String.valueOf(starttls));
        props.put("mail.debug", "false");

        return mailSender;
    }

    /**
     * Get verification URL with token
     */
    public String getVerificationUrlWithToken(String token) {
        return verificationUrl + "?token=" + token;
    }

    /**
     * Get reset password URL with token
     */
    public String getResetPasswordUrlWithToken(String token) {
        return resetPasswordUrl + "?token=" + token;
    }
}