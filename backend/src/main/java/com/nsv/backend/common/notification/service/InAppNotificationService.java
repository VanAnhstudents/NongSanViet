package com.nsv.backend.common.notification.service;

import com.nsv.backend.common.notification.dto.MarkNotificationsReadResponse;
import com.nsv.backend.common.notification.dto.NotificationFeedResponse;
import com.nsv.backend.common.notification.dto.NotificationItemResponse;

public interface InAppNotificationService {
    NotificationItemResponse createNotification(
        Long userId,
        String type,
        String title,
        String content,
        String referenceType,
        Long referenceId
    );

    NotificationItemResponse notifyNewOrder(Long farmerId, Long orderId, String orderSummary);

    NotificationItemResponse notifyOrderStatusChanged(
        Long recipientUserId,
        Long orderId,
        String oldStatus,
        String newStatus
    );

    NotificationItemResponse notifyNewMessage(
        Long recipientUserId,
        String senderName,
        Long messageId,
        String messagePreview
    );

    NotificationFeedResponse getUserNotifications(Long userId, boolean unreadOnly);

    long getUnreadCount(Long userId);

    NotificationItemResponse markAsRead(Long userId, Long notificationId);

    MarkNotificationsReadResponse markAllAsRead(Long userId);
}
