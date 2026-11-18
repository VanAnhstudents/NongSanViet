package com.nsv.backend.trace.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BatchTraceInfoResponse(
    Long batchId,
    UUID traceabilityCode,
    String qrScanUrl,
    LocalDate harvestDate,
    String status,
    BigDecimal unitPrice,
    BigDecimal totalQuantity,
    BigDecimal remainingQuantity
) {}
