package com.nsv.backend.common.notification;

import com.nsv.backend.common.contract.InAppNotificationEvent;
import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.common.notification.dto.MarkNotificationsReadResponse;
import com.nsv.backend.common.notification.dto.NotificationFeedResponse;
import com.nsv.backend.common.notification.dto.NotificationItemResponse;
import com.nsv.backend.common.notification.entity.AppNotification;
import com.nsv.backend.common.notification.repository.AppNotificationRepository;
import com.nsv.backend.common.notification.service.InAppNotificationEventListener;
import com.nsv.backend.common.notification.service.InAppNotificationServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TcNoti01Test {

    @Mock
    private AppNotificationRepository notificationRepository;

    @InjectMocks
    private InAppNotificationServiceImpl notificationService;

    @Test
    @DisplayName("TC-NOTI-01 (1a): Hệ thống tạo thông báo khi có đơn hàng mới (NEW_ORDER) gửi tới Farmer")
    void notifyNewOrder_CreatesUnreadOrderNotificationForFarmer() {
        Long farmerId = 10L;
        Long orderId = 501L;

        when(notificationRepository.save(any(AppNotification.class))).thenAnswer(inv -> {
            AppNotification n = inv.getArgument(0);
            n.setId(1001L);
            n.setCreatedAt(OffsetDateTime.now());
            return n;
        });

        NotificationItemResponse result = notificationService.notifyNewOrder(
            farmerId,
            orderId,
            "Người mua Nguyễn Thế Quyền vừa đặt đơn hàng #501 (2kg Vải thiều Thanh Hà)."
        );

        assertEquals(1001L, result.id());
        assertEquals(farmerId, result.userId());
        assertEquals("NEW_ORDER", result.type());
        assertEquals("Đơn hàng mới #501", result.title());
        assertEquals("ORDER", result.referenceType());
        assertEquals(orderId, result.referenceId());
        assertFalse(result.isRead());
        assertNull(result.readAt());
    }

    @Test
    @DisplayName("TC-NOTI-01 (1b): Hệ thống tạo thông báo khi đơn hàng đổi trạng thái (ORDER_STATUS_CHANGED) gửi tới Buyer")
    void notifyOrderStatusChanged_CreatesStatusUpdateNotificationForBuyer() {
        Long buyerId = 20L;
        Long orderId = 501L;

        when(notificationRepository.save(any(AppNotification.class))).thenAnswer(inv -> {
            AppNotification n = inv.getArgument(0);
            n.setId(1002L);
            n.setCreatedAt(OffsetDateTime.now());
            return n;
        });

        NotificationItemResponse result = notificationService.notifyOrderStatusChanged(
            buyerId,
            orderId,
            "CONFIRMED",
            "SHIPPING"
        );

        assertEquals(1002L, result.id());
        assertEquals(buyerId, result.userId());
        assertEquals("ORDER_STATUS_CHANGED", result.type());
        assertEquals("Đơn hàng #501 - Đang giao hàng", result.title());
        assertTrue(result.content().contains("Đã xác nhận"));
        assertTrue(result.content().contains("Đang giao hàng"));
        assertEquals("ORDER", result.referenceType());
        assertEquals(orderId, result.referenceId());
        assertFalse(result.isRead());
    }

    @Test
    @DisplayName("TC-NOTI-01 (1c): Hệ thống tạo thông báo khi có tin nhắn mới (NEW_MESSAGE) qua sự kiện ứng dụng")
    void onInAppNotificationEvent_NewMessage_PersistsNotification() {
        Long buyerId = 20L;
        Long messageId = 7001L;

        when(notificationRepository.save(any(AppNotification.class))).thenAnswer(inv -> {
            AppNotification n = inv.getArgument(0);
            n.setId(1003L);
            n.setCreatedAt(OffsetDateTime.now());
            return n;
        });

        InAppNotificationEventListener listener = new InAppNotificationEventListener(notificationService);
        InAppNotificationEvent event = InAppNotificationEvent.newMessage(
            buyerId,
            "Trang trại Thanh Sơn",
            messageId,
            "Sáng mai vườn sẽ cắt vải sớm và giao cho đơn vị vận chuyển trước 8h ạ."
        );

        listener.onInAppNotificationEvent(event);

        ArgumentCaptor<AppNotification> captor = ArgumentCaptor.forClass(AppNotification.class);
        verify(notificationRepository, times(1)).save(captor.capture());

        AppNotification saved = captor.getValue();
        assertEquals(buyerId, saved.getUserId());
        assertEquals("NEW_MESSAGE", saved.getType());
        assertEquals("Tin nhắn mới từ Trang trại Thanh Sơn", saved.getTitle());
        assertEquals("MESSAGE", saved.getReferenceType());
        assertEquals(messageId, saved.getReferenceId());
        assertFalse(saved.getIsRead());
    }

    @Test
    @DisplayName("TC-NOTI-01 (2a): Người dùng xem danh sách thông báo và đánh dấu từng thông báo đã đọc")
    void getUserNotificationsAndMarkSingleAsRead_UpdatesReadStateAndUnreadCount() {
        Long buyerId = 20L;
        AppNotification unreadNoti = AppNotification.builder()
            .id(1002L)
            .userId(buyerId)
            .type("ORDER_STATUS_CHANGED")
            .title("Đơn hàng #501 - Đang giao hàng")
            .content("Đơn hàng #501 đang được giao tới bạn.")
            .referenceType("ORDER")
            .referenceId(501L)
            .isRead(false)
            .createdAt(OffsetDateTime.now())
            .build();

        AppNotification readNoti = AppNotification.builder()
            .id(1001L)
            .userId(buyerId)
            .type("NEW_MESSAGE")
            .title("Tin nhắn mới từ Trang trại Thanh Sơn")
            .content("Cảm ơn bạn đã đặt hàng!")
            .referenceType("MESSAGE")
            .referenceId(7000L)
            .isRead(true)
            .readAt(OffsetDateTime.now().minusMinutes(10))
            .createdAt(OffsetDateTime.now().minusMinutes(15))
            .build();

        when(notificationRepository.findAllByUserIdOrderByCreatedAtDescIdDesc(buyerId))
            .thenReturn(List.of(unreadNoti, readNoti));
        when(notificationRepository.countByUserIdAndIsReadFalse(buyerId)).thenReturn(1L);

        // 1. Xem danh sách thông báo
        NotificationFeedResponse feed = notificationService.getUserNotifications(buyerId, false);
        assertEquals(2, feed.totalCount());
        assertEquals(1L, feed.unreadCount());
        assertFalse(feed.items().get(0).isRead());
        assertTrue(feed.items().get(1).isRead());

        // 2. Đánh dấu thông báo 1002L là đã đọc
        when(notificationRepository.findByIdAndUserId(1002L, buyerId)).thenReturn(Optional.of(unreadNoti));
        when(notificationRepository.save(any(AppNotification.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificationItemResponse marked = notificationService.markAsRead(buyerId, 1002L);
        assertTrue(marked.isRead());
        assertNotNull(marked.readAt());
    }

    @Test
    @DisplayName("TC-NOTI-01 (2b): Người dùng đánh dấu tất cả thông báo đã đọc và chặn truy cập thông báo của người khác")
    void markAllAsReadAndOwnershipCheck_WorksCorrectly() {
        Long farmerId = 10L;
        Long strangerUserId = 999L;

        when(notificationRepository.markAllAsReadByUserId(eq(farmerId), any(OffsetDateTime.class))).thenReturn(3);
        when(notificationRepository.countByUserIdAndIsReadFalse(farmerId)).thenReturn(0L);

        MarkNotificationsReadResponse batchResult = notificationService.markAllAsRead(farmerId);
        assertEquals(3, batchResult.updatedCount());
        assertEquals(0L, batchResult.remainingUnreadCount());

        // Chặn người dùng khác đánh dấu đã đọc thông báo không thuộc sở hữu
        when(notificationRepository.findByIdAndUserId(1002L, strangerUserId)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(
            BusinessException.class,
            () -> notificationService.markAsRead(strangerUserId, 1002L)
        );
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("NOTIFICATION_NOT_FOUND", ex.getCode());
    }
}
