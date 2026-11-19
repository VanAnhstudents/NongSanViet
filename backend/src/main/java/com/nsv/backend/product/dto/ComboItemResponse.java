package com.nsv.backend.product.dto;

import java.math.BigDecimal;

public record ComboItemResponse(
    Long productId,
    String productName,
    String unit,
    BigDecimal quantity
) {}
