package com.nsv.backend.order.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record OrderResponse(
    Long id,
    Long buyerId,
    String status,
    String paymentMethod,
    BigDecimal itemsSubtotal,
    BigDecimal shippingFee,
    BigDecimal totalAmount,
    OrderAddressResponse shippingAddress,
    List<OrderItemResponse> items,
    OffsetDateTime deliveredAt,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}
