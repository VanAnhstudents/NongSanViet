package com.nsv.backend.product.dto;

import java.math.BigDecimal;

public record ProductSearchCriteria(
    String keyword,
    Long categoryId,
    Long provinceId,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    Boolean verifiedOnly,
    String sortBy, // NEWEST | PRICE_ASC | PRICE_DESC
    int page,
    int size
) {
    public String normalizedKeyword() {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }

    public String normalizedSortBy() {
        if (sortBy == null || sortBy.isBlank()) {
            return "NEWEST";
        }
        return sortBy.trim().toUpperCase();
    }
}
