package com.nsv.backend.dispute.dto;

import com.nsv.backend.trace.dto.FarmingLogResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record BatchFarmingLogSummary(
    Long batchId,
    Long productId,
    String productName,
    UUID traceabilityCode,
    LocalDate harvestDate,
    List<FarmingLogResponse> logs
) {}
