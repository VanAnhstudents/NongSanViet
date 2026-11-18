package com.nsv.backend.complaint.dto;

import java.time.Instant;

public record FarmerComplaintItem(
        Long id,
        Long orderId,
        String reason,
        Instant createdAt,
        Instant slaDeadline,
        boolean responded,
        boolean escalated,
        String farmerResponse) {
}
