package com.nsv.backend.common.notification.service;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.common.notification.dto.MarkNotificationsReadResponse;
import com.nsv.backend.common.notification.dto.NotificationFeedResponse;
import com.nsv.backend.common.notification.dto.NotificationItemResponse;
import com.nsv.backend.common.notification.entity.AppNotification;
import com.nsv.backend.common.notification.repository.AppNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InAppNotificationServiceImpl implements InAppNotificationService {

    private static final int MAX_MESSAGE_PREVIEW_LENGTH = 120;

    private final AppNotificationRepository notificationRepository;

    @Override
    @Transactional
    public NotificationItemResponse createNotification(
            Long userId,
            String type,
            String title,
            String content,
            String referenceType,
            Long referenceId
    ) {
        if (userId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_RECIPIENT", "Người nhận thông báo không hợp lệ");
        }
        if (!StringUtils.hasText(content)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "EMPTY_NOTIFICATION_CONTENT", "Nội dung thông báo không được để trống");
        }

        String normalizedType = StringUtils.hasText(type) ? type.trim().toUpperCase() : "SYSTEM";
        String normalizedTitle = StringUtils.hasText(title) ? title.trim() : "Thông báo hệ thống";

        AppNotification notification = AppNotification.builder()
            .userId(userId)
            .type(normalizedType)
            .title(normalizedTitle)
            .content(content.trim())
            .referenceType(StringUtils.hasText(referenceType) ? referenceType.trim().toUpperCase() : null)
            .referenceId(referenceId)
            .isRead(false)
            .build();

        AppNotification saved = notificationRepository.save(notification);
        return NotificationItemResponse.from(saved);
    }

    @Override
    @Transactional
    public NotificationItemResponse notifyNewOrder(Long farmerId, Long orderId, String orderSummary) {
        String title = "Đơn hàng mới #" + orderId;
        String content = StringUtils.hasText(orderSummary)
            ? orderSummary.trim()
            : "Bạn có đơn hàng mới #" + orderId + " đang chờ xác nhận.";

        return createNotification(farmerId, "NEW_ORDER", title, content, "ORDER", orderId);
    }

    @Override
    @Transactional
    public NotificationItemResponse notifyOrderStatusChanged(
            Long recipientUserId,
            Long orderId,
            String oldStatus,
            String newStatus
    ) {
        String humanStatus = translateOrderStatus(newStatus);
        String title = "Đơn hàng #" + orderId + " - " + humanStatus;
        String content = String.format(
            "Đơn hàng #%d của bạn đã chuyển trạng thái từ \"%s\" sang \"%s\".",
            orderId,
            translateOrderStatus(oldStatus),
            humanStatus
        );

        return createNotification(recipientUserId, "ORDER_STATUS_CHANGED", title, content, "ORDER", orderId);
    }

    @Override
    @Transactional
    public NotificationItemResponse notifyNewMessage(
            Long recipientUserId,
            String senderName,
            Long messageId,
            String messagePreview
    ) {
        String displaySender = StringUtils.hasText(senderName) ? senderName.trim() : "Người dùng";
        String title = "Tin nhắn mới từ " + displaySender;
        String sanitizedPreview = truncatePreview(messagePreview);
        String content = displaySender + ": " + sanitizedPreview;

        return createNotification(recipientUserId, "NEW_MESSAGE", title, content, "MESSAGE", messageId);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationFeedResponse getUserNotifications(Long userId, boolean unreadOnly) {
        List<AppNotification> notifications = unreadOnly
            ? notificationRepository.findAllByUserIdAndIsReadFalseOrderByCreatedAtDescIdDesc(userId)
            : notificationRepository.findAllByUserIdOrderByCreatedAtDescIdDesc(userId);

        long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(userId);
        List<NotificationItemResponse> items = notifications.stream()
            .map(NotificationItemResponse::from)
            .toList();

        return new NotificationFeedResponse(items, unreadCount, items.size());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public NotificationItemResponse markAsRead(Long userId, Long notificationId) {
        AppNotification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
            .orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND,
                "NOTIFICATION_NOT_FOUND",
                "Không tìm thấy thông báo hoặc bạn không có quyền thao tác"
            ));

        if (!Boolean.TRUE.equals(notification.getIsRead())) {
            notification.setIsRead(true);
            notification.setReadAt(OffsetDateTime.now());
            notification = notificationRepository.save(notification);
        }

        return NotificationItemResponse.from(notification);
    }

    @Override
    @Transactional
    public MarkNotificationsReadResponse markAllAsRead(Long userId) {
        int updated = notificationRepository.markAllAsReadByUserId(userId, OffsetDateTime.now());
        long remainingUnread = notificationRepository.countByUserIdAndIsReadFalse(userId);
        return new MarkNotificationsReadResponse(updated, remainingUnread);
    }

    private String translateOrderStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return "Đang xử lý";
        }
        return switch (status.trim().toUpperCase()) {
            case "PENDING" -> "Chờ xác nhận";
            case "AWAITING_DEPOSIT" -> "Chờ đặt cọc";
            case "DEPOSIT_PAID" -> "Đã đặt cọc giữ chỗ";
            case "CONFIRMED" -> "Đã xác nhận";
            case "SHIPPING" -> "Đang giao hàng";
            case "DELIVERED" -> "Đã giao hàng";
            case "COMPLETED" -> "Hoàn tất";
            case "CANCELLED" -> "Đã hủy";
            default -> status.trim();
        };
    }

    private String truncatePreview(String rawMessage) {
        if (!StringUtils.hasText(rawMessage)) {
            return "[Tệp đính kèm]";
        }
        String trimmed = rawMessage.trim();
        if (trimmed.length() <= MAX_MESSAGE_PREVIEW_LENGTH) {
            return trimmed;
        }
        return trimmed.substring(0, MAX_MESSAGE_PREVIEW_LENGTH) + "...";
    }
}
