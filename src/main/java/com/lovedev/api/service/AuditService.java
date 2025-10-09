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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async
    @Transactional
    public void logAction(User user, AuditAction action, String entityType,
                          String entityId, Map<String, Object> oldValue,
                          Map<String, Object> newValue, String description) {
        try {
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
            log.error("Failed to create audit log", e);
        }
    }

    @Async
    @Transactional
    public void logAction(User user, AuditAction action, String description) {
        logAction(user, action, null, null, null, null, description);
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}