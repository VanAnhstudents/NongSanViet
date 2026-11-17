package com.nsv.backend.order.dto;

import com.nsv.backend.order.entity.PaymentMethod;
import com.nsv.backend.order.entity.PaymentStatus;
import com.nsv.backend.order.entity.PaymentType;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        Long id,
        Long orderId,
        PaymentMethod paymentMethod,
        PaymentType paymentType,
        BigDecimal amount,
        PaymentStatus status,
        String providerRef,
        Instant createdAt) {
}
