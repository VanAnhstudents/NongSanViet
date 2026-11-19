package com.nsv.backend.product.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record ComboResponse(
    Long id,
    Long farmerId,
    String name,
    String description,
    BigDecimal comboPrice,
    String status,
    OffsetDateTime createdAt,
    List<ComboItemResponse> items
) {}
