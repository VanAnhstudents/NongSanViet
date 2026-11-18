package com.nsv.backend.dispute.dto;

import com.nsv.backend.order.dto.CoInspectionResponse;
import com.nsv.backend.order.dto.PaymentResponse;
import com.nsv.backend.order.dto.RefundResponse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record DisputeDetailResponse(
    Long id,
    Long orderId,
    String orderStatus,
    Long buyerId,
    String reason,
    String description,
    String farmerResponse,
    String status,
    String adminDecision,
    String adminNote,
    BigDecimal orderTotal,
    BigDecimal totalPaid,
    BigDecimal refundedAmount,
    Long resolvedBy,
    OffsetDateTime resolvedAt,
    OffsetDateTime createdAt,
    List<ComplaintEvidenceResponse> evidences,
    List<BatchFarmingLogSummary> farmingLogs,
    CoInspectionResponse coInspection,
    List<SystemAuditLogResponse> auditLogs,
    List<PaymentResponse> payments,
    List<RefundResponse> refunds
) {}
