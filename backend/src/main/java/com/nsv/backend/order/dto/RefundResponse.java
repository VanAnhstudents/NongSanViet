package com.nsv.backend.order.dto;

import com.nsv.backend.order.entity.Refund;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record RefundResponse(
    Long id,
    Long paymentId,
    Long complaintId,
    BigDecimal amount,
    String reason,
    String status,
    OffsetDateTime createdAt,
    OffsetDateTime processedAt
) {
    public static RefundResponse from(Refund refund) {
        return new RefundResponse(
            refund.getId(),
            refund.getPaymentId(),
            refund.getComplaintId(),
            refund.getAmount(),
            refund.getReason(),
            refund.getStatus(),
            refund.getCreatedAt(),
            refund.getProcessedAt()
        );
    }
}
