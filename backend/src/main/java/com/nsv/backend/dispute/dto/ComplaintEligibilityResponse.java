package com.nsv.backend.dispute.dto;

import java.time.OffsetDateTime;

public record ComplaintEligibilityResponse(
    Long orderId,
    String orderStatus,
    boolean eligible,
    String reasonCode,
    String message,
    OffsetDateTime deliveredAt,
    OffsetDateTime slaDeadlineAt,
    long remainingSeconds
) {}
