package com.nsv.backend.trace.dto;

public record ProductTraceInfoResponse(
    Long productId,
    String productName,
    String categoryName,
    String unit,
    String description,
    String primaryImageUrl
) {}
