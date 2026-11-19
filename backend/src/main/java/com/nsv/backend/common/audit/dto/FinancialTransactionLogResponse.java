package com.nsv.backend.common.audit.dto;

import com.nsv.backend.common.audit.entity.FinancialTransactionLog;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record FinancialTransactionLogResponse(
    Long id,
    Long orderId,
    Long userId,
    Long actorId,
    String transactionType,
    String paymentMethod,
    BigDecimal amount,
    String status,
    String providerRef,
    String note,
    OffsetDateTime createdAt
) {
    public static FinancialTransactionLogResponse from(FinancialTransactionLog entity) {
        return new FinancialTransactionLogResponse(
            entity.getId(),
            entity.getOrderId(),
            entity.getUserId(),
            entity.getActorId(),
            entity.getTransactionType(),
            entity.getPaymentMethod(),
            entity.getAmount(),
            entity.getStatus(),
            entity.getProviderRef(),
            entity.getNote(),
            entity.getCreatedAt()
        );
    }
}
