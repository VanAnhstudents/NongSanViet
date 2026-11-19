package com.nsv.backend.common.contract;

public record InAppNotificationEvent(
    Long recipientUserId,
    String type,          // NEW_ORDER | ORDER_STATUS_CHANGED | NEW_MESSAGE | DISPUTE_UPDATE | SYSTEM
    String title,
    String content,
    String referenceType, // ORDER | MESSAGE | COMPLAINT | CERTIFICATE
    Long referenceId
) {
    public static InAppNotificationEvent newOrder(Long farmerId, Long orderId, String orderSummary) {
        return new InAppNotificationEvent(
            farmerId,
            "NEW_ORDER",
            "Đơn hàng mới #" + orderId,
            orderSummary,
            "ORDER",
            orderId
        );
    }

    public static InAppNotificationEvent orderStatusChanged(
        Long recipientUserId,
        Long orderId,
        String statusLabel,
        String detailMessage
    ) {
        return new InAppNotificationEvent(
            recipientUserId,
            "ORDER_STATUS_CHANGED",
            "Cập nhật đơn hàng #" + orderId + " - " + statusLabel,
            detailMessage,
            "ORDER",
            orderId
        );
    }

    public static InAppNotificationEvent newMessage(
        Long recipientUserId,
        String senderName,
        Long messageId,
        String previewText
    ) {
        return new InAppNotificationEvent(
            recipientUserId,
            "NEW_MESSAGE",
            "Tin nhắn mới từ " + senderName,
            previewText,
            "MESSAGE",
            messageId
        );
    }
}
