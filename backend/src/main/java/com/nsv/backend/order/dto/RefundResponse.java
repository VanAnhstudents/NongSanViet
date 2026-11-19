package com.nsv.backend.order.dto;

import com.nsv.backend.order.entity.Refund;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public record RefundResponse(
    Long id,
    Long paymentId,
    Long complaintId,
    BigDecimal amount,
    String reason,
    String status,
    OffsetDateTime createdAt
) {
    public static RefundResponse from(Refund entity) {
        return new RefundResponse(
            entity.getId(),
            entity.getPaymentId(),
            entity.getComplaintId(),
            entity.getAmount(),
            entity.getReason(),
            entity.getStatus() != null ? entity.getStatus().name() : null,
            entity.getCreatedAt() != null ? entity.getCreatedAt().atOffset(ZoneOffset.UTC) : null
        );
    }
}
