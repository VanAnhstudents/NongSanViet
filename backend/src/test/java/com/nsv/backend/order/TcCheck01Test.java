package com.nsv.backend.order;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.order.dto.CoInspectionResponse;
import com.nsv.backend.order.dto.CreateCoInspectionRequest;
import com.nsv.backend.order.entity.Order;
import com.nsv.backend.order.entity.OrderCoInspection;
import com.nsv.backend.order.repository.OrderCoInspectionRepository;
import com.nsv.backend.order.repository.OrderRepository;
import com.nsv.backend.order.service.CoInspectionServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TcCheck01Test {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderCoInspectionRepository coInspectionRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private CoInspectionServiceImpl coInspectionService;

    @Test
    @DisplayName("TC-CHECK-01 (1a): Buyer gửi yêu cầu đồng kiểm thành công trước khi xác nhận đã nhận hàng")
    void requestCoInspection_BeforeDelivery_SuccessAndNotifiesFarmer() {
        Long buyerId = 20L;
        Long farmerId = 10L;
        Long orderId = 501L;

        Order order = Order.builder()
            .id(orderId)
            .buyerId(buyerId)
            .status("SHIPPING")
            .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(coInspectionRepository.existsByOrderId(orderId)).thenReturn(false);
        when(coInspectionRepository.save(any(OrderCoInspection.class))).thenAnswer(inv -> {
            OrderCoInspection ci = inv.getArgument(0);
            ci.setId(77L);
            return ci;
        });
        when(orderRepository.findDistinctFarmerIdsByOrderId(orderId)).thenReturn(List.of(farmerId));

        CreateCoInspectionRequest request = new CreateCoInspectionRequest(
            true,
            true,
            "Kiểm tra cân đủ 5kg vải thiều và không bị dập nát"
        );

        CoInspectionResponse response = coInspectionService.requestCoInspection(buyerId, orderId, request);

        assertNotNull(response);
        assertEquals(77L, response.id());
        assertEquals(orderId, response.orderId());
        assertEquals("SHIPPING", response.orderStatus());
        assertEquals("PENDING", response.result());
        assertNotNull(response.notes());
        assertTrue(response.notes().contains("Kiểm tra cân đủ 5kg vải thiều"));

        verify(coInspectionRepository, times(1)).save(any(OrderCoInspection.class));
        verify(jdbcTemplate, times(1)).update(startsWith("INSERT INTO notifications"), eq(farmerId), anyString());
    }

    @Test
    @DisplayName("TC-CHECK-01 (1b): Chặn gửi yêu cầu đồng kiểm nếu đơn hàng đã giao/hoàn tất hoặc đã hủy")
    void requestCoInspection_AlreadyDeliveredOrCompleted_ThrowsBadRequest() {
        Long buyerId = 20L;
        Long orderId = 502L;

        Order completedOrder = Order.builder()
            .id(orderId)
            .buyerId(buyerId)
            .status("COMPLETED")
            .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(completedOrder));

        BusinessException ex = assertThrows(
            BusinessException.class,
            () -> coInspectionService.requestCoInspection(
                buyerId,
                orderId,
                new CreateCoInspectionRequest(true, true, "Kiểm tra hàng")
            )
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("INVALID_ORDER_STATUS_FOR_CO_INSPECTION", ex.getCode());
        verify(coInspectionRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-CHECK-01 (2): Yêu cầu đồng kiểm hiển thị cho Farmer và Farmer xác nhận phối hợp")
    void farmerViewsAndAcknowledgesCoInspection_Success() {
        Long farmerId = 10L;
        Long buyerId = 20L;
        Long orderId = 501L;

        Order order = Order.builder()
            .id(orderId)
            .buyerId(buyerId)
            .status("CONFIRMED")
            .build();

        OrderCoInspection savedCi = OrderCoInspection.builder()
            .id(77L)
            .orderId(orderId)
            .buyerId(buyerId)
            .result("PENDING")
            .notes("Cân kiểm tra trọng lượng tại chỗ")
            .locked(false)
            .build();

        when(coInspectionRepository.findAllByFarmerId(farmerId)).thenReturn(List.of(savedCi));
        when(orderRepository.findAllById(Set.of(orderId))).thenReturn(List.of(order));

        List<CoInspectionResponse> farmerList = coInspectionService.getFarmerCoInspections(farmerId);
        assertEquals(1, farmerList.size());
        assertEquals(orderId, farmerList.get(0).orderId());
        assertEquals("PENDING", farmerList.get(0).result());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.existsOrderForFarmer(orderId, farmerId)).thenReturn(true);
        when(coInspectionRepository.findByOrderId(orderId)).thenReturn(Optional.of(savedCi));
        when(coInspectionRepository.save(any(OrderCoInspection.class))).thenAnswer(inv -> inv.getArgument(0));

        CoInspectionResponse acknowledged = coInspectionService.acknowledgeCoInspection(farmerId, orderId);
        assertEquals("ACKNOWLEDGED", acknowledged.result());
        assertNotNull(acknowledged.confirmedAt());

        verify(jdbcTemplate, times(1)).update(startsWith("INSERT INTO notifications"), eq(buyerId), anyString());
    }
}
