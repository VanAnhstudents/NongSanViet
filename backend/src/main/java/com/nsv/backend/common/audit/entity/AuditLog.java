package com.nsv.backend.common.audit.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_id", nullable = false)
    private Long actorId;

    @Column(name = "actor_role", nullable = false, length = 20)
    @Builder.Default
    private String actorRole = "ADMIN";

    @Column(name = "category", nullable = false, length = 30)
    @Builder.Default
    private String category = "ADMIN_ACTION"; // ADMIN_ACTION | FINANCIAL_TX | SYSTEM_OPERATION

    @Column(name = "action", nullable = false, length = 60)
    private String action; // LOCK_USER_ACCOUNT | UNLOCK_USER_ACCOUNT | APPROVE_REFUND | REMOVE_PRODUCT | ...

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType; // users | refunds | products | orders | payments

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "target_user_id")
    private Long targetUserId;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
