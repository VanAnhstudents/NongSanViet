package com.nsv.backend.order.dto;

import com.nsv.backend.order.entity.RefundStatus;

import java.math.BigDecimal;

public record RefundItem(Long refundId, Long paymentId, BigDecimal amount, RefundStatus status) {
}
