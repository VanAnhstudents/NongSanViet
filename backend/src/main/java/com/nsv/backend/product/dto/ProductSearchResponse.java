package com.nsv.backend.product.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductSearchResponse(
    List<ProductSummaryResponse> items,
    long totalElements,
    int totalPages,
    int page,
    int size,
    AppliedFiltersSummary appliedFilters
) {
    public record AppliedFiltersSummary(
        String keyword,
        Long categoryId,
        Long provinceId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        boolean verifiedOnly,
        String sortBy
    ) {}
}
