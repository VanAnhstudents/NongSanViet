package com.nsv.backend.common.contract;

import com.nsv.backend.common.audit.service.AuditLogService;
import com.nsv.backend.common.notification.service.InAppNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class JdbcAuditAndNotificationAdapter implements AuditAndNotificationPort {

    private final InAppNotificationService notificationService;
    private final AuditLogService auditLogService;
    private final JdbcTemplate jdbcTemplate;
    private final AuditLogService auditLogService;

    @Override
    public void sendNotification(Long userId, String content) {
        notificationService.createNotification(userId, "SYSTEM", "Thông báo hệ thống", content, null, null);
    }

    @Override
    public void sendStructuredNotification(
            Long userId,
            String type,
            String title,
            String content,
            String referenceType,
            Long referenceId
    ) {
        notificationService.createNotification(userId, type, title, content, referenceType, referenceId);
    }

    @Override
    public void notifyNewOrder(Long farmerId, Long orderId, String orderSummary) {
        notificationService.notifyNewOrder(farmerId, orderId, orderSummary);
    }

    @Override
    public void notifyOrderStatusChanged(Long recipientUserId, Long orderId, String oldStatus, String newStatus) {
        notificationService.notifyOrderStatusChanged(recipientUserId, orderId, oldStatus, newStatus);
    }

    @Override
    public void notifyNewMessage(Long recipientUserId, String senderName, Long messageId, String messagePreview) {
        notificationService.notifyNewMessage(recipientUserId, senderName, messageId, messagePreview);
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
