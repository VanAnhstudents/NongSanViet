package com.nsv.backend.dispute.dto;

import java.time.OffsetDateTime;

public record SystemAuditLogResponse(
    Long id,
    Long actorId,
    String action,
    String entityType,
    Long entityId,
    OffsetDateTime createdAt
) {}
