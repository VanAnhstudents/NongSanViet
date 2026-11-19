package com.nsv.backend.order;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.inventory.entity.Batch;
import com.nsv.backend.inventory.repository.BatchRepository;
import com.nsv.backend.order.dto.OrderResponse;
import com.nsv.backend.order.dto.UpdateOrderStatusRequest;
import com.nsv.backend.order.entity.Order;
import com.nsv.backend.order.entity.OrderAddress;
import com.nsv.backend.order.entity.OrderItem;
import com.nsv.backend.order.repository.CartItemRepository;
import com.nsv.backend.order.repository.OrderRepository;
import com.nsv.backend.order.service.OrderServiceImpl;
import com.nsv.backend.product.entity.Product;
import com.nsv.backend.product.repository.ProductRepository;
import com.nsv.backend.user.repository.AddressRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TcOrd03Test {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order buildSampleOrder(Long orderId, Long buyerId, String status) {
        Order order = Order.builder()
            .id(orderId)
            .buyerId(buyerId)
            .paymentMethod("COD")
            .status(status)
            .shippingFee(new BigDecimal("15000.00"))
            .build();

        order.addItem(OrderItem.builder()
            .id(1L)
            .batchId(101L)
            .quantity(new BigDecimal("2.00"))
            .unitPrice(new BigDecimal("35000.00"))
            .build());

        order.attachAddress(OrderAddress.builder()
            .receiverName("Nguyễn Thế Quyền")
            .receiverPhone("0987654321")
            .provinceName("Hải Phòng")
            .addressLine("Kim Thành, Hải Phòng")
            .build());

        return order;
    }

    @Test
    @DisplayName("TC-ORD-03 (1): Farmer xem được danh sách đơn hàng của mình kèm trạng thái hiện tại")
    void getFarmerOrders_ReturnsOwnedOrdersWithCurrentStatus() {
        Long farmerId = 10L;
        Order order1 = buildSampleOrder(501L, 20L, "PENDING");
        Order order2 = buildSampleOrder(502L, 21L, "SHIPPING");

        Batch batch = Batch.builder().id(101L).productId(1L).unitPrice(new BigDecimal("35000.00")).build();
        Product product = Product.builder().id(1L).farmerId(farmerId).name("Vải thiều Thanh Hà").unit("kg").build();

        when(orderRepository.findAllByFarmerIdAndOptionalStatus(farmerId, null))
            .thenReturn(List.of(order1, order2));
        when(batchRepository.findAllByIdIn(Set.of(101L))).thenReturn(List.of(batch));
        when(productRepository.findAllByIdIn(Set.of(1L))).thenReturn(List.of(product));

        List<OrderResponse> results = orderService.getFarmerOrders(farmerId, null);

        assertEquals(2, results.size());
        assertEquals(501L, results.get(0).id());
        assertEquals("PENDING", results.get(0).status());
        assertEquals(502L, results.get(1).id());
        assertEquals("SHIPPING", results.get(1).status());
    }

    @Test
    @DisplayName("TC-ORD-03 (2a): Cập nhật trạng thái tuần tự PENDING -> CONFIRMED -> SHIPPING -> DELIVERED và Buyer thấy ngay")
    void updateOrderStatus_ValidSequentialFlow_SuccessAndVisibleToBuyer() {
        Long farmerId = 10L;
        Long buyerId = 20L;
        Long orderId = 501L;

        Order order = buildSampleOrder(orderId, buyerId, "PENDING");
        Batch batch = Batch.builder().id(101L).productId(1L).unitPrice(new BigDecimal("35000.00")).build();
        Product product = Product.builder().id(1L).farmerId(farmerId).name("Vải thiều Thanh Hà").unit("kg").build();

        when(orderRepository.findWithDetailsById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.existsOrderForFarmer(orderId, farmerId)).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(batchRepository.findAllByIdIn(Set.of(101L))).thenReturn(List.of(batch));
        when(productRepository.findAllByIdIn(Set.of(1L))).thenReturn(List.of(product));
        when(orderRepository.findWithDetailsByIdAndBuyerId(orderId, buyerId)).thenReturn(Optional.of(order));

        // Bước 1: PENDING -> CONFIRMED (Đang xử lý)
        OrderResponse step1 = orderService.updateOrderStatusByFarmer(
            farmerId, orderId, new UpdateOrderStatusRequest("CONFIRMED")
        );
        assertEquals("CONFIRMED", step1.status());
        assertNull(step1.deliveredAt());

        // Bước 2: CONFIRMED -> SHIPPING (Đang giao)
        OrderResponse step2 = orderService.updateOrderStatusByFarmer(
            farmerId, orderId, new UpdateOrderStatusRequest("SHIPPING")
        );
        assertEquals("SHIPPING", step2.status());

        // Bước 3: SHIPPING -> DELIVERED (Đã giao) -> tự động gán mốc deliveredAt cho SLA 24h
        OrderResponse step3 = orderService.updateOrderStatusByFarmer(
            farmerId, orderId, new UpdateOrderStatusRequest("DELIVERED")
        );
        assertEquals("DELIVERED", step3.status());
        assertNotNull(step3.deliveredAt());

        // Kiểm tra Buyer xem chi tiết đơn thấy trạng thái DELIVERED ngay lập tức
        OrderResponse buyerView = orderService.getOrderDetail(buyerId, orderId);
        assertEquals("DELIVERED", buyerView.status());
        assertNotNull(buyerView.deliveredAt());

        verify(jdbcTemplate, times(3)).update(startsWith("INSERT INTO notifications"), eq(buyerId), anyString());
        verify(jdbcTemplate, times(3)).update(startsWith("INSERT INTO audit_logs"), eq(farmerId), eq("UPDATE_ORDER_STATUS"), eq("orders"), eq(orderId));
    }

    @Test
    @DisplayName("TC-ORD-03 (2b): Chặn nhảy cóc trạng thái không hợp lệ (ví dụ PENDING -> SHIPPING hoặc PENDING -> DELIVERED)")
    void updateOrderStatus_InvalidStatusJump_ThrowsBadRequest() {
        Long farmerId = 10L;
        Long orderId = 501L;
        Order order = buildSampleOrder(orderId, 20L, "PENDING");

        when(orderRepository.findWithDetailsById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.existsOrderForFarmer(orderId, farmerId)).thenReturn(true);

        BusinessException jumpToShipping = assertThrows(
            BusinessException.class,
            () -> orderService.updateOrderStatusByFarmer(farmerId, orderId, new UpdateOrderStatusRequest("SHIPPING"))
        );
        assertEquals(HttpStatus.BAD_REQUEST, jumpToShipping.getStatus());
        assertEquals("INVALID_STATUS_TRANSITION", jumpToShipping.getCode());
        assertEquals("PENDING", order.getStatus());

        BusinessException jumpToDelivered = assertThrows(
            BusinessException.class,
            () -> orderService.updateOrderStatusByFarmer(farmerId, orderId, new UpdateOrderStatusRequest("DELIVERED"))
        );
        assertEquals("INVALID_STATUS_TRANSITION", jumpToDelivered.getCode());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-ORD-03 (2c): Chặn Farmer cập nhật đơn hàng không thuộc trang trại của mình (403 Forbidden)")
    void updateOrderStatus_OtherFarmerOrder_ThrowsForbidden() {
        Long otherFarmerId = 99L;
        Long orderId = 501L;
        Order order = buildSampleOrder(orderId, 20L, "PENDING");

        when(orderRepository.findWithDetailsById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.existsOrderForFarmer(orderId, otherFarmerId)).thenReturn(false);

        BusinessException ex = assertThrows(
            BusinessException.class,
            () -> orderService.updateOrderStatusByFarmer(otherFarmerId, orderId, new UpdateOrderStatusRequest("CONFIRMED"))
        );
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("ORDER_ACCESS_DENIED", ex.getCode());
        verify(orderRepository, never()).save(any());
    }
}
