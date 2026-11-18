package com.nsv.backend.trace.dto;

public record FarmTraceInfoResponse(
    Long farmId,
    String farmName,
    String farmerName,
    String provinceName,
    String addressLine,
    String certificateStatus,
    String certificateUrl
) {}
