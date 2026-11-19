package com.nsv.backend.common.notification.dto;

import java.util.List;

public record NotificationFeedResponse(
    List<NotificationItemResponse> items,
    long unreadCount,
    int totalCount
) {}
