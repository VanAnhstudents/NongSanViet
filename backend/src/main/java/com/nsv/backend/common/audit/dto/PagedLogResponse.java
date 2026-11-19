package com.nsv.backend.common.audit.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public record PagedLogResponse<T>(
    List<T> items,
    long totalElements,
    int totalPages,
    int page,
    int size
) {
    public static <E, T> PagedLogResponse<T> fromPage(Page<E> pageData, Function<E, T> mapper) {
        List<T> mapped = pageData.getContent().stream().map(mapper).toList();
        return new PagedLogResponse<>(
            mapped,
            pageData.getTotalElements(),
            pageData.getTotalPages(),
            pageData.getNumber(),
            pageData.getSize()
        );
    }
}
