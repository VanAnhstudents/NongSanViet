package com.nsv.backend.common.notification.dto;

public record MarkNotificationsReadResponse(
    int updatedCount,
    long remainingUnreadCount
) {}
