package com.nsv.backend.trace.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record TraceabilityResponse(
    BatchTraceInfoResponse batch,
    ProductTraceInfoResponse product,
    FarmTraceInfoResponse farm,
    List<FarmingLogResponse> farmingLogs,
    int totalLogs,
    OffsetDateTime verifiedAt
) {}
