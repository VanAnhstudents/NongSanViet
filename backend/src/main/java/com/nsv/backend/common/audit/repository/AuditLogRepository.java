package com.nsv.backend.common.audit.repository;

import com.nsv.backend.common.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("""
        SELECT a FROM AuditLog a
        WHERE (:actorId IS NULL OR a.actorId = :actorId)
          AND (:category IS NULL OR a.category = :category)
          AND (:action IS NULL OR a.action = :action)
          AND (:entityType IS NULL OR a.entityType = :entityType)
          AND (:targetUserId IS NULL OR a.targetUserId = :targetUserId)
          AND (:orderId IS NULL OR a.orderId = :orderId)
          AND (:fromTime IS NULL OR a.createdAt >= :fromTime)
          AND (:toTime IS NULL OR a.createdAt <= :toTime)
        ORDER BY a.createdAt DESC, a.id DESC
        """)
    Page<AuditLog> searchLogs(
        @Param("actorId") Long actorId,
        @Param("category") String category,
        @Param("action") String action,
        @Param("entityType") String entityType,
        @Param("targetUserId") Long targetUserId,
        @Param("orderId") Long orderId,
        @Param("fromTime") OffsetDateTime fromTime,
        @Param("toTime") OffsetDateTime toTime,
        Pageable pageable
    );
}
