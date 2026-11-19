package com.nsv.backend.common.contract;

<<<<<<< Updated upstream
=======
import com.nsv.backend.common.audit.service.AuditLogService;
import com.nsv.backend.common.notification.service.InAppNotificationService;
>>>>>>> Stashed changes
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class JdbcAuditAndNotificationAdapter implements AuditAndNotificationPort {

<<<<<<< Updated upstream
=======
    private final InAppNotificationService notificationService;
    private final AuditLogService auditLogService;
>>>>>>> Stashed changes
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void sendNotification(Long userId, String content) {
        jdbcTemplate.update(
            "INSERT INTO notifications (user_id, content, is_read) VALUES (?, ?, FALSE)",
            userId,
            content
        );
    }

    @Override
    public void recordAuditLog(Long actorId, String action, String entityType, Long entityId) {
        jdbcTemplate.update(
            "INSERT INTO audit_logs (actor_id, action, entity_type, entity_id) VALUES (?, ?, ?, ?)",
            actorId,
            action,
            entityType,
            entityId
        );
    }

    @Override
    public void recordAdminAction(
            Long adminId,
            String action,
            String entityType,
            Long entityId,
            Long targetUserId,
            Long orderId,
            String details
    ) {
        auditLogService.recordAdminAction(adminId, action, entityType, entityId, targetUserId, orderId, details);
    }

    @Override
    public void recordFinancialTransaction(
            Long orderId,
            Long userId,
            Long actorId,
            String transactionType,
            String paymentMethod,
            BigDecimal amount,
            String status,
            String providerRef,
            String note
    ) {
        auditLogService.recordFinancialTransaction(
            orderId, userId, actorId, transactionType, paymentMethod, amount, status, providerRef, note
        );
    }
}
