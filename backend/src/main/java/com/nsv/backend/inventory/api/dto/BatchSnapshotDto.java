package com.nsv.backend.inventory.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BatchSnapshotDto(
    Long batchId,
    Long productId,
    UUID traceabilityCode,
    LocalDate harvestDate,
    BigDecimal unitPrice,
    BigDecimal totalQuantity,
    BigDecimal remainingQuantity,
    String status
) {}
