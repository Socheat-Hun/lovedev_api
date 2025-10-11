package com.lovedev.api.service;

import com.lovedev.api.model.entity.AuditLog;
import com.lovedev.api.model.entity.User;
import com.lovedev.api.model.enums.AuditAction;
import com.lovedev.api.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Log action synchronously (within same transaction)
     * Use this for actions within a transaction (like registration)
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void logAction(User user, AuditAction action, String entityType,
                          String entityId, Map<String, Object> oldValue,
                          Map<String, Object> newValue, String description) {
        try {
            // Validate user exists
            if (user == null || user.getId() == null) {
                log.warn("Cannot log action: user is null or has no ID");
                return;
            }

            HttpServletRequest request = getCurrentRequest();

            AuditLog auditLog = AuditLog.builder()
                    .user(user)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .description(description)
                    .ipAddress(request != null ? getClientIp(request) : null)
                    .userAgent(request != null ? request.getHeader("User-Agent") : null)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} - {} - {}", user.getEmail(), action, entityType);

        } catch (Exception e) {
            // Log but don't throw - audit logging should never break main flow
            log.error("Failed to create audit log for user: {} - action: {}",
                    user != null ? user.getEmail() : "unknown", action, e);
        }
    }

    /**
     * Simplified log action with description only
     */
    public void logAction(User user, AuditAction action, String description) {
        logAction(user, action, null, null, null, null, description);
    }

    /**
     * Log action asynchronously (use after transaction commits)
     * Use this for non-critical logging that can happen later
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logActionAsync(User user, AuditAction action, String description) {
        logAction(user, action, null, null, null, null, description);
    }

    /**
     * Get current HTTP request from context
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            log.debug("Could not get current request: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get client IP address (handles proxies)
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}