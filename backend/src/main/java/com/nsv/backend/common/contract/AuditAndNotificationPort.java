package com.nsv.backend.common.contract;

public interface AuditAndNotificationPort {
    void sendNotification(Long userId, String content);

    void sendStructuredNotification(
        Long userId,
        String type,
        String title,
        String content,
        String referenceType,
        Long referenceId
    );

    void notifyNewOrder(Long farmerId, Long orderId, String orderSummary);

    void notifyOrderStatusChanged(Long recipientUserId, Long orderId, String oldStatus, String newStatus);

    void notifyNewMessage(Long recipientUserId, String senderName, Long messageId, String messagePreview);

    void recordAuditLog(Long actorId, String action, String entityType, Long entityId);
}
