package com.nsv.backend.common.audit.dto;

import com.nsv.backend.common.audit.entity.AuditLog;

import java.time.OffsetDateTime;

public record AuditLogResponse(
    Long id,
    Long actorId,
    String actorRole,
    String category,
    String action,
    String entityType,
    Long entityId,
    Long orderId,
    Long targetUserId,
    String details,
    OffsetDateTime createdAt
) {
    public static AuditLogResponse from(AuditLog entity) {
        return new AuditLogResponse(
            entity.getId(),
            entity.getActorId(),
            entity.getActorRole(),
            entity.getCategory(),
            entity.getAction(),
            entity.getEntityType(),
            entity.getEntityId(),
            entity.getOrderId(),
            entity.getTargetUserId(),
            entity.getDetails(),
            entity.getCreatedAt()
        );
    }
}
