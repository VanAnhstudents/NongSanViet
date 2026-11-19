package com.nsv.backend.product.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ProductSummaryResponse(
    Long id,
    String name,
    Long categoryId,
    String categoryName,
    String unit,
    String description,
    String primaryImageUrl,
    List<String> imageUrls,
    Long batchId,
    UUID traceabilityCode,
    BigDecimal unitPrice,
    BigDecimal remainingQuantity,
    String batchStatus,
    LocalDate harvestDate,
    Long farmId,
    String farmName,
    String provinceName,
    boolean farmVerified,
    OffsetDateTime createdAt
) {}
