package com.nsv.backend.order.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record PreOrderResponse(
    Long id,
    Long buyerId,
    String orderType,
    String orderTypeLabel,
    String status,
    String statusLabel,
    String paymentMethod,
    LocalDate expectedHarvestDate,
    BigDecimal itemsSubtotal,
    BigDecimal shippingFee,
    BigDecimal totalAmount,
    BigDecimal depositRate,
    BigDecimal requiredDepositAmount,
    BigDecimal remainingAmount,
    boolean depositPaid,
    OrderAddressResponse shippingAddress,
    List<OrderItemResponse> items,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}
