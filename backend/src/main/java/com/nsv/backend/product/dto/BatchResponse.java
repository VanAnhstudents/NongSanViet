package com.nsv.backend.product.dto;

import com.nsv.backend.product.entity.BatchStatus;

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
        BatchStatus status,
        Instant createdAt) {
}
