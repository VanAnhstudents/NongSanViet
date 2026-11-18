package com.nsv.backend.complaint.dto;

import java.time.Instant;

public record EscalatedComplaintItem(
        Long id,
        Long orderId,
        String reason,
        Instant createdAt,
        Instant escalatedAt,
        String farmerResponse,
        String adminDecision) {
}
