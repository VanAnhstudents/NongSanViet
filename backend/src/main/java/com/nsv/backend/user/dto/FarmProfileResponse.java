package com.nsv.backend.user.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record FarmProfileResponse(
    Long id,
    Long farmerId,
    String farmerName,
    String farmName,
    String description,
    String provinceName,
    String addressLine,
    String mainCategoryName,
    String certificateStatus,
    boolean verified,
    int verifiedBadgeCount,
    List<VerifiedBadgeResponse> verifiedBadges,
    OffsetDateTime createdAt
) {}
