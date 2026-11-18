package com.nsv.backend.complaint.dto;

import java.time.Instant;

/** Kết quả phản hồi: thời điểm phản hồi được ghi nhận và so với hạn SLA. */
public record ComplaintSlaResponse(
        Long complaintId,
        String farmerResponse,
        Instant createdAt,
        Instant slaDeadline,
        Instant respondedAt,
        boolean withinSla) {
}
