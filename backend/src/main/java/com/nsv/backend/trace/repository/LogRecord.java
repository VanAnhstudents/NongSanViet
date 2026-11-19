package com.nsv.backend.trace.repository;

import java.time.LocalDate;

/** Một dòng nhật ký canh tác kèm mã truy xuất của lô (dữ liệu gắn với mã QR). */
public record LogRecord(Long id, Long batchId, String traceabilityCode, LocalDate logDate,
        String activity, String note, String imageUrl) {
}
