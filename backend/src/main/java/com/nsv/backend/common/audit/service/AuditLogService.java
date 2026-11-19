package com.nsv.backend.common.audit.service;

import com.nsv.backend.common.audit.dto.AuditLogResponse;
import com.nsv.backend.common.audit.dto.FinancialTransactionLogResponse;
import com.nsv.backend.common.audit.dto.PagedLogResponse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public interface AuditLogService {

    AuditLogResponse recordAdminAction(
        Long adminId,
        String action,
        String entityType,
        Long entityId,
        Long targetUserId,
        Long orderId,
        String details
    );

    AuditLogResponse logUserAccountLockChange(
        Long adminId,
        Long targetUserId,
        boolean locked,
        String reason
    );

    AuditLogResponse logAdminRefundApproval(
        Long adminId,
        Long orderId,
        Long refundId,
        Long recipientUserId,
        BigDecimal refundAmount,
        String reason
    );

    AuditLogResponse logAdminProductRemoval(
        Long adminId,
        Long productId,
        Long farmerId,
        String reason
    );

    FinancialTransactionLogResponse recordFinancialTransaction(
        Long orderId,
        Long userId,
        Long actorId,
        String transactionType,
        String paymentMethod,
        BigDecimal amount,
        String status,
        String providerRef,
        String note
    );

    PagedLogResponse<AuditLogResponse> searchAdminAuditLogs(
        Long actorId,
        String category,
        String action,
        String entityType,
        Long targetUserId,
        Long orderId,
        OffsetDateTime fromTime,
        OffsetDateTime toTime,
        int page,
        int size
    );

    PagedLogResponse<FinancialTransactionLogResponse> searchFinancialLogs(
        Long orderId,
        Long userId,
        String transactionType,
        String status,
        OffsetDateTime fromTime,
        OffsetDateTime toTime,
        int page,
        int size
    );

    List<FinancialTransactionLogResponse> getFinancialLogsByOrderId(Long orderId);

    List<FinancialTransactionLogResponse> getFinancialLogsByUserId(Long userId);
}
