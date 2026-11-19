package com.nsv.backend.dispute.dto;

import com.nsv.backend.dispute.entity.Complaint;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record ComplaintSubmissionResponse(
    Long id,
    Long orderId,
    Long buyerId,
    String reason,
    String description,
    String status,
    String farmerResponse,
    String adminDecision,
    String adminNote,
    BigDecimal refundAmount,
    OffsetDateTime deliveredAt,
    OffsetDateTime slaDeadlineAt,
    OffsetDateTime createdAt,
    List<ComplaintEvidenceResponse> evidences
) {
    public static ComplaintSubmissionResponse from(Complaint complaint, OffsetDateTime deliveredAt) {
        List<ComplaintEvidenceResponse> evidenceList = complaint.getEvidences().stream()
            .map(ComplaintEvidenceResponse::from)
            .toList();

        return new ComplaintSubmissionResponse(
            complaint.getId(),
            complaint.getOrderId(),
            complaint.getBuyerId(),
            complaint.getReason(),
            complaint.getDescription(),
            complaint.getStatus(),
            complaint.getFarmerResponse(),
            complaint.getAdminDecision(),
            complaint.getAdminNote(),
            complaint.getRefundAmount(),
            deliveredAt,
            complaint.getSlaDeadlineAt(),
            complaint.getCreatedAt(),
            evidenceList
        );
    }
}
