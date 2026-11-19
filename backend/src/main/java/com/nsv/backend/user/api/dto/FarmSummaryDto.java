package com.nsv.backend.user.api.dto;

public record FarmSummaryDto(
    Long farmId,
    Long farmerId,
    String farmerName,
    String farmName,
    Long provinceId,
    String provinceName,
    String addressLine,
    boolean verified,
    String certificateUrl
) {}
