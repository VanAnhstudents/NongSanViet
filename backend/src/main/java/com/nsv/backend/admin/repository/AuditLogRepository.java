package com.nsv.backend.admin.repository;

import com.nsv.backend.admin.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    boolean existsByActionAndEntityTypeAndEntityId(String action, String entityType, Long entityId);

    List<AuditLog> findByActionAndEntityTypeOrderByIdDesc(String action, String entityType);
}
