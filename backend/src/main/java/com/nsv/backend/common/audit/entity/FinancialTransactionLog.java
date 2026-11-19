package com.nsv.backend.common.audit.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "financial_transaction_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialTransactionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "actor_id", nullable = false)
    private Long actorId;

    @Column(name = "transaction_type", nullable = false, length = 40)
    private String transactionType; // DEPOSIT | FULL_PAYMENT | COD_SETTLEMENT | REFUND | DISPUTE_REFUND

    @Column(name = "payment_method", nullable = false, length = 30)
    private String paymentMethod; // ONLINE | COD | WALLET_REFUND

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // SUCCESS | FAILED | REVERSED

    @Column(name = "provider_ref", length = 120)
    private String providerRef;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
