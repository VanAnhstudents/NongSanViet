package com.nsv.backend.product.dto;

import java.util.List;

public record SearchFilterOptionsResponse(
    List<FilterOptionItem> categories,
    List<FilterOptionItem> provinces
) {
    public record FilterOptionItem(
        Long id,
        String name
    ) {}
}
