package com.nsv.backend.order.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
    Long id,
    Long batchId,
    Long productId,
    String productName,
    String unit,
    BigDecimal quantity,
    BigDecimal unitPrice,
    BigDecimal subtotal
) {}
