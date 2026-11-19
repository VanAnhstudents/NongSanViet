package com.nsv.backend.order;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.order.dto.CoInspectionResponse;
import com.nsv.backend.order.dto.ConfirmOrderDeliveryRequest;
import com.nsv.backend.order.dto.RecordCoInspectionRequest;
import com.nsv.backend.order.entity.Order;
import com.nsv.backend.order.entity.OrderCoInspection;
import com.nsv.backend.order.repository.OrderCoInspectionRepository;
import com.nsv.backend.order.repository.OrderRepository;
import com.nsv.backend.order.service.OrderCoInspectionServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderCoInspectionServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderCoInspectionRepository coInspectionRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private OrderCoInspectionServiceImpl coInspectionService;

    @Test
    @DisplayName("TC-CHECK-02 (1): Buyer ghi nhận đồng kiểm ĐẠT (PASSED) kèm ghi chú và xác nhận 'Đã giao' -> đơn hàng chuyển sang COMPLETED và bị khóa")
    void recordCoInspectionPassedAndConfirmDelivery_CompletesOrderAndLocksResult() {
        Long buyerId = 20L;
        Long farmerId = 10L;
        Long orderId = 601L;

        Order shippingOrder = Order.builder()
            .id(orderId)
            .buyerId(buyerId)
            .paymentMethod("COD")
            .status("SHIPPING")
            .shippingFee(new BigDecimal("15000.00"))
            .build();

        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(shippingOrder));
        when(coInspectionRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(coInspectionRepository.save(any(OrderCoInspection.class))).thenAnswer(inv -> {
            OrderCoInspection ci = inv.getArgument(0);
            if (ci.getId() == null) {
                ci.setId(301L);
            }
            return ci;
        });
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.findDistinctFarmerIdsByOrderId(orderId)).thenReturn(List.of(farmerId));

        RecordCoInspectionRequest request = new RecordCoInspectionRequest(
            "PASSED",
            "Hàng tươi nguyên vẹn, đủ trọng lượng 5kg theo đơn",
            true
        );

        CoInspectionResponse response = coInspectionService.recordCoInspection(buyerId, orderId, request);

        assertNotNull(response);
        assertEquals(301L, response.id());
        assertEquals(orderId, response.orderId());
        assertEquals("PASSED", response.result());
        assertTrue(response.passed());
        assertEquals("Hàng tươi nguyên vẹn, đủ trọng lượng 5kg theo đơn", response.notes());
        assertTrue(response.locked(), "Kết quả đồng kiểm phải bị khóa ngay sau khi xác nhận hoàn tất");
        assertNotNull(response.confirmedAt());

        // Trạng thái đơn hàng chuyển đúng sang COMPLETED và ghi nhận thời điểm giao
        assertEquals("COMPLETED", shippingOrder.getStatus());
        assertEquals("COMPLETED", response.orderStatus());
        assertNotNull(shippingOrder.getDeliveredAt());

        verify(jdbcTemplate, times(1)).update(startsWith("INSERT INTO notifications"), eq(farmerId), anyString());
        verify(jdbcTemplate, times(1)).update(
            startsWith("INSERT INTO audit_logs"),
            eq(buyerId),
            eq("CONFIRM_CO_INSPECTION_PASSED"),
            eq("orders"),
            eq(orderId)
        );
    }

    @Test
    @DisplayName("TC-CHECK-02 (2): Sau khi xác nhận 'Đã giao' và hoàn tất đơn hàng, không thể chỉnh sửa lại kết quả đồng kiểm")
    void editCoInspectionAfterOrderCompleted_ThrowsConflictLocked() {
        Long buyerId = 20L;
        Long orderId = 601L;

        Order completedOrder = Order.builder()
            .id(orderId)
            .buyerId(buyerId)
            .status("COMPLETED")
            .deliveredAt(OffsetDateTime.now().minusMinutes(5))
            .build();

        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(completedOrder));

        RecordCoInspectionRequest tamperRequest = new RecordCoInspectionRequest(
            "FAILED",
            "Thử sửa lại thành không đạt sau khi đã hoàn tất",
            true
        );

        BusinessException ex = assertThrows(
            BusinessException.class,
            () -> coInspectionService.recordCoInspection(buyerId, orderId, tamperRequest)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("CO_INSPECTION_LOCKED", ex.getCode());
        verify(coInspectionRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-CHECK-02 (3): Quy trình 2 bước (Lưu nháp đồng kiểm -> Xác nhận 'Đã giao') khóa kết quả sau khi xác nhận")
    void twoStepCoInspectionAndConfirmDelivery_LocksOnConfirmation() {
        Long buyerId = 20L;
        Long farmerId = 10L;
        Long orderId = 602L;

        Order shippingOrder = Order.builder()
            .id(orderId)
            .buyerId(buyerId)
            .status("SHIPPING")
            .build();

        OrderCoInspection draftInspection = OrderCoInspection.builder()
            .id(302L)
            .orderId(orderId)
            .buyerId(buyerId)
            .result("PASSED")
            .notes("Đang kiểm tra bao bì")
            .locked(false)
            .build();

        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(shippingOrder));
        when(coInspectionRepository.findByOrderId(orderId)).thenReturn(Optional.of(draftInspection));
        when(coInspectionRepository.save(any(OrderCoInspection.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.findDistinctFarmerIdsByOrderId(orderId)).thenReturn(List.of(farmerId));

        // Xác nhận "Đã giao" chốt kết quả đồng kiểm
        CoInspectionResponse confirmed = coInspectionService.confirmDeliveryAndComplete(
            buyerId,
            orderId,
            new ConfirmOrderDeliveryRequest("Đã kiểm tra đủ số lượng và xác nhận nhận hàng")
        );

        assertEquals("COMPLETED", shippingOrder.getStatus());
        assertEquals("COMPLETED", confirmed.orderStatus());
        assertTrue(confirmed.locked());
        assertEquals("Đã kiểm tra đủ số lượng và xác nhận nhận hàng", confirmed.notes());

        // Cố gắng xác nhận lại lần 2 -> 409 Conflict
        BusinessException duplicateConfirm = assertThrows(
            BusinessException.class,
            () -> coInspectionService.confirmDeliveryAndComplete(buyerId, orderId, null)
        );
        assertEquals(HttpStatus.CONFLICT, duplicateConfirm.getStatus());
        assertEquals("CO_INSPECTION_LOCKED", duplicateConfirm.getCode());
    }

    @Test
    @DisplayName("TC-CHECK-02 (4): Đồng kiểm KHÔNG ĐẠT (FAILED) bắt buộc có ghi chú và chuyển đơn sang DELIVERED để mở cổng khiếu nại 24h")
    void recordCoInspectionFailed_RequiresNotesAndSetsOrderToDeliveredForComplaintWindow() {
        Long buyerId = 20L;
        Long farmerId = 10L;
        Long orderId = 603L;

        Order shippingOrder = Order.builder()
            .id(orderId)
            .buyerId(buyerId)
            .status("SHIPPING")
            .build();

        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(shippingOrder));

        // 1. Thiếu ghi chú khi chọn FAILED -> 400 Bad Request
        BusinessException missingNotesEx = assertThrows(
            BusinessException.class,
            () -> coInspectionService.recordCoInspection(
                buyerId,
                orderId,
                new RecordCoInspectionRequest("FAILED", "   ", true)
            )
        );
        assertEquals(HttpStatus.BAD_REQUEST, missingNotesEx.getStatus());
        assertEquals("CO_INSPECTION_NOTES_REQUIRED", missingNotesEx.getCode());

        // 2. Nhập đầy đủ lý do không đạt -> khóa phiếu đồng kiểm và chuyển đơn sang DELIVERED
        when(coInspectionRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(coInspectionRepository.save(any(OrderCoInspection.class))).thenAnswer(inv -> {
            OrderCoInspection ci = inv.getArgument(0);
            ci.setId(303L);
            return ci;
        });
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.findDistinctFarmerIdsByOrderId(orderId)).thenReturn(List.of(farmerId));

        CoInspectionResponse failedResult = coInspectionService.recordCoInspection(
            buyerId,
            orderId,
            new RecordCoInspectionRequest("FAILED", "Thùng hàng móp méo, 30% trái bị dập", true)
        );

        assertEquals("FAILED", failedResult.result());
        assertFalse(failedResult.passed());
        assertTrue(failedResult.locked());
        assertEquals("DELIVERED", shippingOrder.getStatus());
        assertNotNull(shippingOrder.getDeliveredAt());
    }

    @Test
    @DisplayName("TC-CHECK-02 (5): Chặn người dùng không phải chủ đơn hàng hoặc đơn chưa giao thực hiện đồng kiểm")
    void recordCoInspection_WrongBuyerOrPendingOrder_ThrowsForbiddenOrBadRequest() {
        Long ownerBuyerId = 20L;
        Long strangerBuyerId = 999L;
        Long orderId = 604L;

        Order pendingOrder = Order.builder()
            .id(orderId)
            .buyerId(ownerBuyerId)
            .status("PENDING")
            .build();

        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(pendingOrder));

        RecordCoInspectionRequest validReq = new RecordCoInspectionRequest("PASSED", "OK", true);

        // 1. Buyer khác cố tình thao tác -> 403 Forbidden
        BusinessException forbiddenEx = assertThrows(
            BusinessException.class,
            () -> coInspectionService.recordCoInspection(strangerBuyerId, orderId, validReq)
        );
        assertEquals(HttpStatus.FORBIDDEN, forbiddenEx.getStatus());
        assertEquals("ORDER_ACCESS_DENIED", forbiddenEx.getCode());

        // 2. Đơn hàng chưa ở trạng thái SHIPPING/DELIVERED -> 400 Bad Request
        BusinessException badStatusEx = assertThrows(
            BusinessException.class,
            () -> coInspectionService.recordCoInspection(ownerBuyerId, orderId, validReq)
        );
        assertEquals(HttpStatus.BAD_REQUEST, badStatusEx.getStatus());
        assertEquals("INVALID_ORDER_STATUS_FOR_CO_INSPECTION", badStatusEx.getCode());
    }
}
