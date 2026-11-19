package com.nsv.backend.order.dto;

import com.nsv.backend.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

public record ShortageRefundResponse(Long orderId, OrderStatus orderStatus, BigDecimal totalRefunded,
        List<RefundItem> refunds) {
}
