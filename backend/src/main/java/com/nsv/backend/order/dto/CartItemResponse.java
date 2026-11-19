package com.nsv.backend.order.dto;

import java.math.BigDecimal;

public record CartItemResponse(
    Long id,
    Long batchId,
    Long productId,
    String productName,
    String unit,
    BigDecimal unitPrice,
    BigDecimal quantity,
    BigDecimal subtotal,
    BigDecimal availableStock
) {}
