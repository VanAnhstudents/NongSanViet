package com.nsv.backend.order.dto;

import java.math.BigDecimal;
import java.util.List;

public record ShortageRefundResponse(Long orderId, String orderStatus, BigDecimal totalRefunded,
        List<RefundItem> refunds) {
}
