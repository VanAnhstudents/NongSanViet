package com.nsv.backend.order.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderDisputeContextDto(
    Long orderId,
    Long buyerId,
    String orderStatus,
    String paymentMethod,
    BigDecimal orderTotal,
    BigDecimal totalPaid,
    List<Long> batchIds,
    List<Long> farmerIds
) {}
