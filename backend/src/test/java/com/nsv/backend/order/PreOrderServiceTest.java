package com.nsv.backend.order;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.inventory.entity.Batch;
import com.nsv.backend.inventory.repository.BatchRepository;
import com.nsv.backend.order.dto.*;
import com.nsv.backend.order.entity.Order;
import com.nsv.backend.order.entity.OrderAddress;
import com.nsv.backend.order.entity.OrderItem;
import com.nsv.backend.order.repository.OrderRepository;
import com.nsv.backend.order.service.FinancialTransactionService;
import com.nsv.backend.order.service.PreOrderServiceImpl;
import com.nsv.backend.product.entity.Product;
import com.nsv.backend.product.repository.ProductRepository;
import com.nsv.backend.user.entity.Address;
import com.nsv.backend.user.entity.Province;
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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PreOrderServiceTest {

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private FinancialTransactionService financialTransactionService;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private PreOrderServiceImpl preOrderService;

    @Test
    @DisplayName("TC-ORD-02 (1a): Buyer đặt trước thành công cho lô sản phẩm gắn lịch thu hoạch và nhận yêu cầu đặt cọc 30%")
    void createPreOrder_ScheduledHarvestBatch_CreatesOrderWithRequiredDeposit() {
        Long buyerId = 20L;
        Long batchId = 301L;
        LocalDate harvestDate = LocalDate.of(2026, 7, 15);

        Address address = Address.builder()
            .id(5L)
            .userId(buyerId)
            .province(new Province(30L, "Hải Phòng"))
            .receiverName("Nguyễn Thế Quyền")
            .receiverPhone("0987654321")
            .addressLine("Kim Thành, Hải Phòng")
            .build();

        Batch preOrderBatch = Batch.builder()
            .id(batchId)
            .productId(1L)
            .traceabilityCode(UUID.randomUUID())
            .harvestDate(harvestDate)
            .unitPrice(new BigDecimal("50000.00"))
            .totalQuantity(new BigDecimal("200.00"))
            .remainingQuantity(new BigDecimal("200.00"))
            .status("PRE_ORDER")
            .build();

        Product product = Product.builder()
            .id(1L)
            .farmerId(10L)
            .name("Nhãn lồng Hưng Yên đầu mùa")
            .unit("kg")
            .build();

        when(addressRepository.findByIdAndUser(5L, buyerId)).thenReturn(Optional.of(address));
        when(jdbcTemplate.queryForObject(anyString(), eq(String.class), eq(30L))).thenReturn("Hải Phòng");
        when(batchRepository.findAllByIdInOrderByIdAscForUpdate(List.of(batchId))).thenReturn(List.of(preOrderBatch));
        when(productRepository.findAllByIdIn(Set.of(1L))).thenReturn(List.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(801L);
            return o;
        });
        when(orderRepository.findDistinctFarmerIdsByOrderId(801L)).thenReturn(List.of(10L));

        // Đặt trước 3kg * 50,000 + 20,000 ship = 170,000 VND; đặt cọc 30% = 51,000 VND; còn lại = 119,000 VND
        CreatePreOrderRequest request = new CreatePreOrderRequest(
            batchId,
            new BigDecimal("3.00"),
            5L,
            new BigDecimal("20000.00"),
            new BigDecimal("0.30")
        );

        PreOrderResponse response = preOrderService.createPreOrder(buyerId, request);

        assertNotNull(response);
        assertEquals(801L, response.id());
        assertEquals("PRE_ORDER", response.orderType());
        assertEquals("AWAITING_DEPOSIT", response.status());
        assertTrue(response.statusLabel().contains("Chờ thanh toán tiền cọc giữ chỗ"));
        assertEquals(harvestDate, response.expectedHarvestDate());
        assertEquals(new BigDecimal("170000.00"), response.totalAmount());
        assertEquals(new BigDecimal("51000.00"), response.requiredDepositAmount());
        assertEquals(new BigDecimal("119000.00"), response.remainingAmount());
        assertFalse(response.depositPaid());

        // Kiểm tra giữ chỗ trừ sản lượng dự kiến trên lô PRE_ORDER
        assertEquals(new BigDecimal("197.00"), preOrderBatch.getRemainingQuantity());
        verify(batchRepository, times(1)).save(preOrderBatch);
    }

    @Test
    @DisplayName("TC-ORD-02 (1b): Chặn đặt trước nếu lô hàng không phải PRE_ORDER hoặc thiếu lịch thu hoạch")
    void createPreOrder_StandardBatchOrMissingHarvestDate_ThrowsBadRequest() {
        Long buyerId = 20L;
        Address address = Address.builder()
            .id(5L)
            .userId(buyerId)
            .province(new Province(30L, "Hải Phòng"))
            .build();
        Batch regularBatch = Batch.builder()
            .id(302L)
            .productId(1L)
            .harvestDate(LocalDate.of(2026, 6, 1))
            .unitPrice(new BigDecimal("40000.00"))
            .remainingQuantity(new BigDecimal("50.00"))
            .status("AVAILABLE") // Hàng có sẵn, không phải lô đặt trước
            .build();

        when(addressRepository.findByIdAndUser(5L, buyerId)).thenReturn(Optional.of(address));
        when(jdbcTemplate.queryForObject(anyString(), eq(String.class), eq(30L))).thenReturn("Hải Phòng");
        when(batchRepository.findAllByIdInOrderByIdAscForUpdate(List.of(302L))).thenReturn(List.of(regularBatch));

        BusinessException ex = assertThrows(
            BusinessException.class,
            () -> preOrderService.createPreOrder(
                buyerId,
                new CreatePreOrderRequest(302L, new BigDecimal("2.00"), 5L, BigDecimal.ZERO, new BigDecimal("0.30"))
            )
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("BATCH_NOT_PRE_ORDER", ex.getCode());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-ORD-02 (2): Thanh toán tiền cọc chuyển đơn Pre-order sang trạng thái riêng DEPOSIT_PAID để giữ chỗ")
    void payPreOrderDeposit_AwaitingDepositOrder_TransitionsToDepositPaidWithDistinctLabel() {
        Long buyerId = 20L;
        Long orderId = 801L;
        LocalDate harvestDate = LocalDate.of(2026, 7, 15);

        Order preOrder = Order.builder()
            .id(orderId)
            .buyerId(buyerId)
            .orderType("PRE_ORDER")
            .paymentMethod("ONLINE")
            .status("AWAITING_DEPOSIT")
            .shippingFee(new BigDecimal("20000.00"))
            .depositRate(new BigDecimal("0.3000"))
            .depositAmount(new BigDecimal("51000.00"))
            .remainingAmount(new BigDecimal("119000.00"))
            .expectedHarvestDate(harvestDate)
            .build();

        preOrder.addItem(OrderItem.builder()
            .id(1L)
            .batchId(301L)
            .quantity(new BigDecimal("3.00"))
            .unitPrice(new BigDecimal("50000.00"))
            .build());

        preOrder.attachAddress(OrderAddress.builder()
            .receiverName("Nguyễn Thế Quyền")
            .receiverPhone("0987654321")
            .provinceName("Hải Phòng")
            .addressLine("Kim Thành, Hải Phòng")
            .build());

        Batch batch = Batch.builder()
            .id(301L)
            .productId(1L)
            .harvestDate(harvestDate)
            .unitPrice(new BigDecimal("50000.00"))
            .build();
        Product product = Product.builder().id(1L).farmerId(10L).name("Nhãn lồng Hưng Yên đầu mùa").unit("kg").build();

        when(orderRepository.findWithDetailsByIdAndBuyerId(orderId, buyerId)).thenReturn(Optional.of(preOrder));
        when(financialTransactionService.processPayment(eq(buyerId), eq(false), any(ProcessPaymentRequest.class)))
            .thenReturn(new PaymentResponse(
                901L,
                orderId,
                "DEPOSIT",
                new BigDecimal("51000.00"),
                new BigDecimal("170000.00"),
                "PAID",
                "SIM-PAY-801-DEPOSIT",
                OffsetDateTime.now()
            ));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.findDistinctFarmerIdsByOrderId(orderId)).thenReturn(List.of(10L));
        when(batchRepository.findAllByIdIn(Set.of(301L))).thenReturn(List.of(batch));
        when(productRepository.findAllByIdIn(Set.of(1L))).thenReturn(List.of(product));

        PreOrderResponse confirmedPreOrder = preOrderService.payPreOrderDeposit(
            buyerId, orderId, new PayDepositRequest("SIM-PAY-801-DEPOSIT")
        );

        assertEquals("PRE_ORDER", confirmedPreOrder.orderType());
        assertEquals("DEPOSIT_PAID", confirmedPreOrder.status());
        assertTrue(confirmedPreOrder.statusLabel().contains("Đã đặt cọc giữ chỗ"));
        assertTrue(confirmedPreOrder.statusLabel().contains("2026-07-15"));
        assertTrue(confirmedPreOrder.depositPaid());
        assertEquals(new BigDecimal("51000.00"), confirmedPreOrder.requiredDepositAmount());
        assertEquals(new BigDecimal("119000.00"), confirmedPreOrder.remainingAmount());

        verify(jdbcTemplate, times(1)).update(startsWith("INSERT INTO notifications"), eq(buyerId), anyString());
        verify(jdbcTemplate, times(1)).update(startsWith("INSERT INTO notifications"), eq(10L), anyString());
    }
}
