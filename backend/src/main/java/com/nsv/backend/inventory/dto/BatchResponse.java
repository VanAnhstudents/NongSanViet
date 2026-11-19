package com.nsv.backend.inventory.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BatchResponse(
        Long id,
        Long productId,
        String productName,
        String unit,
        UUID traceabilityCode,
        LocalDate harvestDate,
        BigDecimal unitPrice,
        BigDecimal totalQuantity,
        String status,
        Instant createdAt) {
}
