package com.nsv.backend.order;

import com.nsv.backend.order.dto.ShortageRefundResponse;
import com.nsv.backend.order.entity.Order;
import com.nsv.backend.order.entity.OrderStatus;
import com.nsv.backend.order.entity.Payment;
import com.nsv.backend.order.entity.PaymentStatus;
import com.nsv.backend.order.entity.PaymentType;
import com.nsv.backend.order.entity.Refund;
import com.nsv.backend.order.entity.RefundStatus;
import com.nsv.backend.order.repository.OrderRepository;
import com.nsv.backend.order.repository.RefundPaymentRepository;
import com.nsv.backend.order.repository.RefundQueryRepository;
import com.nsv.backend.order.repository.RefundRepository;
import com.nsv.backend.order.service.BuyerNotifier;
import com.nsv.backend.order.service.PreorderRefundService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PreorderRefundServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-17T03:00:00Z");
    private static final long FARMER_ID = 10L;
    private static final long BUYER_ID = 5L;
    private static final long ORDER_ID = 1L;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RefundPaymentRepository paymentRepository;

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private RefundQueryRepository refundQueryRepository;

    @Mock
    private BuyerNotifier buyerNotifier;

    private Order order;
    private PreorderRefundService service;

    @BeforeEach
    void setUp() {
        service = new PreorderRefundService(orderRepository, paymentRepository, refundRepository,
                refundQueryRepository, buyerNotifier, Clock.fixed(NOW, ZoneId.of("Asia/Ho_Chi_Minh")));

        order = new Order();
        order.setId(ORDER_ID);
        order.setBuyerId(BUYER_ID);
        order.setStatus(OrderStatus.CONFIRMED.name());

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(refundQueryRepository.orderBelongsToFarmer(ORDER_ID, FARMER_ID)).thenReturn(true);
        when(refundQueryRepository.orderHasOtherFarmers(ORDER_ID, FARMER_ID)).thenReturn(false);
        when(refundQueryRepository.isPreOrder(ORDER_ID)).thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(refundRepository.save(any(Refund.class))).thenAnswer(inv -> {
            Refund r = inv.getArgument(0);
            r.setId(100L);
            return r;
        });
    }

    private Payment paidDeposit() {
        return Payment.builder().id(7L).orderId(ORDER_ID).paymentType(PaymentType.DEPOSIT.name())
                .amount(new BigDecimal("60000.00")).status(PaymentStatus.PAID.name()).createdAt(NOW.atOffset(ZoneOffset.UTC)).build();
    }

    @Test
    @DisplayName("TC-REFUND-01: Farmer báo thiếu hàng Pre-order -> tự động tạo giao dịch hoàn tiền cọc tương ứng")
    void tc01_createsRefundForPaidDeposit() {
        Payment deposit = paidDeposit();
        when(paymentRepository.findByOrderIdAndStatus(ORDER_ID, PaymentStatus.PAID)).thenReturn(List.of(deposit));

        ShortageRefundResponse result = service.reportShortage(FARMER_ID, ORDER_ID, "Mất mùa do bão");

        ArgumentCaptor<Refund> saved = ArgumentCaptor.forClass(Refund.class);
        verify(refundRepository).save(saved.capture());
        assertThat(saved.getValue().getPaymentId()).isEqualTo(7L);
        assertThat(saved.getValue().getAmount()).isEqualByComparingTo("60000");
        assertThat(saved.getValue().getStatus()).isEqualTo(RefundStatus.COMPLETED);
        assertThat(saved.getValue().getComplaintId()).isNull();
        assertThat(saved.getValue().getReason()).contains("Mất mùa do bão");
        assertThat(deposit.getStatus()).isEqualTo(PaymentStatus.REFUNDED.name());
        assertThat(result.totalRefunded()).isEqualByComparingTo("60000");
        assertThat(result.refunds()).hasSize(1);
    }

    @Test
    @DisplayName("TC-REFUND-01: Buyer nhận thông báo hoàn tiền và đơn hàng chuyển sang Đã hủy")
    void tc01_buyerNotifiedAndOrderCancelled() {
        when(paymentRepository.findByOrderIdAndStatus(ORDER_ID, PaymentStatus.PAID)).thenReturn(List.of(paidDeposit()));

        ShortageRefundResponse result = service.reportShortage(FARMER_ID, ORDER_ID, "Mất mùa do bão");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED.name());
        assertThat(order.getUpdatedAt()).isEqualTo(NOW.atOffset(ZoneOffset.UTC));
        assertThat(result.orderStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(buyerNotifier).notifyBuyer(eq(BUYER_ID), contains("đã được hoàn lại"));
    }

    @Test
    @DisplayName("TC-REFUND-01: Đơn chưa có khoản nào đã thanh toán -> vẫn hủy đơn và báo Buyer, không tạo giao dịch hoàn")
    void tc01_noPaidPaymentCancelsWithoutRefund() {
        when(paymentRepository.findByOrderIdAndStatus(ORDER_ID, PaymentStatus.PAID)).thenReturn(List.of());

        ShortageRefundResponse result = service.reportShortage(FARMER_ID, ORDER_ID, "Mất mùa do bão");

        verify(refundRepository, never()).save(any(Refund.class));
        assertThat(result.totalRefunded()).isEqualByComparingTo("0");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED.name());
        verify(buyerNotifier).notifyBuyer(eq(BUYER_ID), anyString());
    }
}
