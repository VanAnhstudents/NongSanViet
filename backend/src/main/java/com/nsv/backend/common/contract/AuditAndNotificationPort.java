package com.nsv.backend.common.contract;

import java.math.BigDecimal;

public interface AuditAndNotificationPort {
    void sendNotification(Long userId, String content);
    void recordAuditLog(Long actorId, String action, String entityType, Long entityId);

    void recordAdminAction(
        Long adminId,
        String action,
        String entityType,
        Long entityId,
        Long targetUserId,
        Long orderId,
        String details
    );

    void recordFinancialTransaction(
        Long orderId,
        Long userId,
        Long actorId,
        String transactionType,
        String paymentMethod,
        BigDecimal amount,
        String status,
        String providerRef,
        String note
    );
}
