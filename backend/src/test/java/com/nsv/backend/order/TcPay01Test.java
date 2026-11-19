package com.nsv.backend.order;

import com.nsv.backend.order.dto.PaymentResponse;
import com.nsv.backend.order.dto.SimulatedOutcome;
import com.nsv.backend.order.entity.Order;
import com.nsv.backend.order.entity.Payment;
import com.nsv.backend.order.entity.PaymentMethod;
import com.nsv.backend.order.repository.OrderItemRepository;
import com.nsv.backend.order.repository.OrderRepository;
import com.nsv.backend.order.repository.PaymentRepository;
import com.nsv.backend.order.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TcPay01Test {

    private static final long BUYER_ID = 10L;
    private static final long ORDER_ID = 1L;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private PaymentRepository paymentRepository;

    private Order order;
    private PaymentService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-17T03:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
        service = new PaymentService(orderRepository, orderItemRepository, paymentRepository, clock);

        order = new Order();
        order.setId(ORDER_ID);
        order.setBuyerId(BUYER_ID);
        order.setStatus("PENDING");
        order.setShippingFee(new BigDecimal("20000"));

        when(orderRepository.findByIdAndBuyerId(ORDER_ID, BUYER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderItemRepository.sumAmountByOrderId(ORDER_ID)).thenReturn(new BigDecimal("150000.00"));
        when(paymentRepository.findFirstByOrderIdOrderByIdDesc(ORDER_ID)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            if (p.getId() == null) {
                p.setId(1L);
            }
            return p;
        });
    }

    private Payment pendingPayment() {
        return Payment.builder()
                .id(1L)
                .orderId(ORDER_ID)
                .paymentType("FULL")
                .amount(new BigDecimal("170000.00"))
                .status("PENDING")
                .createdAt(OffsetDateTime.ofInstant(Instant.parse("2026-11-17T03:00:00Z"), ZoneOffset.UTC))
                .build();
    }

    @Test
    @DisplayName("TC-PAY-01: Buyer chọn COD -> giao dịch FULL, chưa thanh toán (PENDING), tổng gồm phí vận chuyển")
    void tc01_chooseCod() {
        PaymentResponse result = service.choosePaymentMethod(BUYER_ID, ORDER_ID, PaymentMethod.COD);

        assertThat(result.paymentType()).isEqualTo("FULL");
        assertThat(result.status()).isEqualTo("PENDING");
        assertThat(result.amount()).isEqualByComparingTo("170000");
        assertThat(order.getPaymentMethod()).isEqualTo("COD");
    }

    @Test
    @DisplayName("TC-PAY-01: Buyer chọn thanh toán online mô phỏng -> giao dịch PENDING, chờ hoàn tất mô phỏng")
    void tc01_chooseOnline() {
        PaymentResponse result = service.choosePaymentMethod(BUYER_ID, ORDER_ID, PaymentMethod.ONLINE);

        assertThat(result.status()).isEqualTo("PENDING");
        assertThat(order.getPaymentMethod()).isEqualTo("ONLINE");
    }

    @Test
    @DisplayName("TC-PAY-01: Giao dịch mô phỏng thành công -> trạng thái PAID, có mã giao dịch")
    void tc01_simulateSuccessIsPaid() {
        order.setPaymentMethod("ONLINE");
        Payment payment = pendingPayment();
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        PaymentResponse result = service.simulateOnlinePayment(BUYER_ID, 1L, SimulatedOutcome.SUCCESS);

        assertThat(result.status()).isEqualTo("PAID");
        assertThat(result.providerRef()).startsWith("SIM-");
        verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("TC-PAY-01: Giao dịch mô phỏng thất bại -> trạng thái FAILED (chưa thanh toán)")
    void tc01_simulateFailureIsFailed() {
        order.setPaymentMethod("ONLINE");
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(pendingPayment()));

        PaymentResponse result = service.simulateOnlinePayment(BUYER_ID, 1L, SimulatedOutcome.FAILED);

        assertThat(result.status()).isEqualTo("FAILED");
    }
}
