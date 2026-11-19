package com.nsv.backend.common.notification.dto;

import com.nsv.backend.common.notification.entity.AppNotification;

import java.time.OffsetDateTime;

public record NotificationItemResponse(
    Long id,
    Long userId,
    String type,
    String title,
    String content,
    String referenceType,
    Long referenceId,
    boolean isRead,
    OffsetDateTime readAt,
    OffsetDateTime createdAt
) {
    public static NotificationItemResponse from(AppNotification entity) {
        return new NotificationItemResponse(
            entity.getId(),
            entity.getUserId(),
            entity.getType(),
            entity.getTitle(),
            entity.getContent(),
            entity.getReferenceType(),
            entity.getReferenceId(),
            Boolean.TRUE.equals(entity.getIsRead()),
            entity.getReadAt(),
            entity.getCreatedAt()
        );
    }
}
