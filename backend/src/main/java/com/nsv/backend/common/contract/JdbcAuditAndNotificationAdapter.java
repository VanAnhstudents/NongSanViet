package com.nsv.backend.common.contract;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JdbcAuditAndNotificationAdapter implements AuditAndNotificationPort {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void sendNotification(Long userId, String content) {
        jdbcTemplate.update(
            "INSERT INTO notifications (user_id, content, is_read) VALUES (?, ?, FALSE)",
            userId,
            content
        );
    }

    @Override
    public void recordAuditLog(Long actorId, String action, String entityType, Long entityId) {
        jdbcTemplate.update(
            "INSERT INTO audit_logs (actor_id, action, entity_type, entity_id) VALUES (?, ?, ?, ?)",
            actorId,
            action,
            entityType,
            entityId
        );
    }
}
