package com.nsv.backend.order.dto;

import com.nsv.backend.order.entity.Payment;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PaymentResponse(
    Long id,
    Long orderId,
    String paymentType,
    BigDecimal amount,
    BigDecimal orderTotal,
    String status,
    String providerRef,
    OffsetDateTime createdAt
) {
    public static PaymentResponse from(Payment payment, BigDecimal orderTotal) {
        return new PaymentResponse(
            payment.getId(),
            payment.getOrderId(),
            payment.getPaymentType(),
            payment.getAmount(),
            orderTotal,
            payment.getStatus(),
            payment.getProviderRef(),
            payment.getCreatedAt()
        );
    }
}
