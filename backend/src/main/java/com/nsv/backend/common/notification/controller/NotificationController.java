package com.nsv.backend.common.notification.controller;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.common.notification.dto.MarkNotificationsReadResponse;
import com.nsv.backend.common.notification.dto.NotificationFeedResponse;
import com.nsv.backend.common.notification.dto.NotificationItemResponse;
import com.nsv.backend.common.notification.service.InAppNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@PreAuthorize("hasAnyRole('BUYER', 'FARMER', 'ADMIN')")
@RequiredArgsConstructor
public class NotificationController {

    private final InAppNotificationService notificationService;

    // Farmer / Buyer xem danh sách thông báo trong ứng dụng (hỗ trợ lọc chỉ thông báo chưa đọc)
    @GetMapping
    public ResponseEntity<NotificationFeedResponse> getMyNotifications(
            Authentication authentication,
            @RequestParam(name = "unreadOnly", defaultValue = "false") boolean unreadOnly
    ) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(notificationService.getUserNotifications(userId, unreadOnly));
    }

    // Lấy nhanh số lượng thông báo chưa đọc để hiển thị badge trên thanh điều hướng
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(Authentication authentication) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(Map.of("unreadCount", notificationService.getUnreadCount(userId)));
    }

    // Đánh dấu 1 thông báo là đã đọc
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationItemResponse> markNotificationAsRead(
            Authentication authentication,
            @PathVariable Long notificationId
    ) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(notificationService.markAsRead(userId, notificationId));
    }

    // Đánh dấu tất cả thông báo của người dùng là đã đọc
    @PatchMapping("/read-all")
    public ResponseEntity<MarkNotificationsReadResponse> markAllNotificationsAsRead(Authentication authentication) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(notificationService.markAllAsRead(userId));
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Chưa xác thực người dùng");
        }
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException ex) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN_SUBJECT", "Thông tin định danh không hợp lệ");
        }
    }
}
