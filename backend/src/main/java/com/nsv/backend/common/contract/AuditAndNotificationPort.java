package com.nsv.backend.common.contract;

public interface AuditAndNotificationPort {
    void sendNotification(Long userId, String content);
    void recordAuditLog(Long actorId, String action, String entityType, Long entityId);
}
