package com.nsv.backend.product.api.dto;

public record ProductBriefDto(
    Long productId,
    Long farmerId,
    String productName,
    String categoryName,
    String unit,
    String description,
    String primaryImageUrl
) {}
