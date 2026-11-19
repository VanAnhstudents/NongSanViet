package com.nsv.backend.order.dto;

import java.math.BigDecimal;

public record RefundItem(Long refundId, Long paymentId, BigDecimal amount, String status) {
}
