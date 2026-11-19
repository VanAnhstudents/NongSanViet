package com.nsv.backend.order.service;

import com.nsv.backend.common.exception.ApiException;
import com.nsv.backend.order.dto.PaymentResponse;
import com.nsv.backend.order.dto.SimulatedOutcome;
import com.nsv.backend.order.entity.Order;
import com.nsv.backend.order.entity.Payment;
import com.nsv.backend.order.entity.PaymentMethod;
import com.nsv.backend.order.repository.OrderItemRepository;
import com.nsv.backend.order.repository.OrderRepository;
import com.nsv.backend.order.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * PBI-18: chọn hình thức thanh toán (COD / ONLINE mô phỏng) và cập nhật trạng thái thanh toán.
 * COD: giao dịch ở trạng thái PENDING (chưa thanh toán) cho tới khi giao hàng.
 * ONLINE: PENDING cho tới khi hoàn tất giao dịch mô phỏng, sau đó PAID hoặc FAILED.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final Clock clock;

    @Transactional
    public PaymentResponse choosePaymentMethod(Long buyerId, Long orderId, PaymentMethod method) {
        Order order = findOwnedOrder(buyerId, orderId);
        if (!"PENDING".equals(order.getStatus())) {
            throw invalidState("Chỉ chọn được hình thức thanh toán khi đơn hàng đang chờ xử lý (PENDING)");
        }
        Optional<Payment> latest = paymentRepository.findFirstByOrderIdOrderByIdDesc(orderId);
        if (latest.isPresent() && !"FAILED".equals(latest.get().getStatus())) {
            throw invalidState("Đơn hàng đã có giao dịch thanh toán");
        }
        BigDecimal amount = totalOf(order);
        if (amount.signum() <= 0) {
            throw invalidState("Đơn hàng không có giá trị cần thanh toán");
        }

        order.setPaymentMethod(method.name());
        order.setUpdatedAt(OffsetDateTime.now(clock));
        orderRepository.save(order);

        Payment payment = Payment.builder()
                .orderId(orderId)
                .paymentType("FULL")
                .amount(amount)
                .status("PENDING")
                .createdAt(OffsetDateTime.now(clock))
                .build();
        return toResponse(paymentRepository.save(payment), order);
    }

    /** Hoàn tất giao dịch trực tuyến mô phỏng: SUCCESS -> PAID, FAILED -> FAILED. */
    @Transactional
    public PaymentResponse simulateOnlinePayment(Long buyerId, Long paymentId, SimulatedOutcome outcome) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy giao dịch thanh toán"));
        Order order = findOwnedOrder(buyerId, payment.getOrderId());
        if (!"ONLINE".equals(order.getPaymentMethod())) {
            throw invalidState("Chỉ giao dịch thanh toán trực tuyến mới cần hoàn tất mô phỏng");
        }
        if (!"PENDING".equals(payment.getStatus())) {
            throw invalidState("Giao dịch này đã được xử lý");
        }
        payment.setStatus(outcome == SimulatedOutcome.SUCCESS ? "PAID" : "FAILED");
        payment.setProviderRef("SIM-" + UUID.randomUUID());
        return toResponse(paymentRepository.save(payment), order);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getLatest(Long buyerId, Long orderId) {
        Order order = findOwnedOrder(buyerId, orderId);
        Payment payment = paymentRepository.findFirstByOrderIdOrderByIdDesc(orderId)
                .orElseThrow(() -> ApiException.notFound("Đơn hàng chưa có giao dịch thanh toán"));
        return toResponse(payment, order);
    }

    private Order findOwnedOrder(Long buyerId, Long orderId) {
        return orderRepository.findByIdAndBuyerId(orderId, buyerId)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy đơn hàng của bạn"));
    }

    private BigDecimal totalOf(Order order) {
        BigDecimal items = orderItemRepository.sumAmountByOrderId(order.getId());
        BigDecimal shipping = order.getShippingFee() == null ? BigDecimal.ZERO : order.getShippingFee();
        return (items == null ? BigDecimal.ZERO : items).add(shipping);
    }

    private ApiException invalidState(String message) {
        return new ApiException(HttpStatus.CONFLICT, "INVALID_STATE", message, null);
    }

    private PaymentResponse toResponse(Payment p, Order order) {
        return PaymentResponse.from(p, totalOf(order));
    }
}
