package com.nsv.backend.admin.service;

import com.nsv.backend.admin.entity.AuditLog;
import com.nsv.backend.admin.repository.AdminAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AdminAuditLogRepository auditLogRepository;
    private final Clock clock;

    /** actorId = null nghĩa là hệ thống tự thực hiện. */
    @Transactional
    public void record(Long actorId, String action, String entityType, Long entityId) {
        auditLogRepository.save(AuditLog.builder()
                .actorId(actorId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .createdAt(Instant.now(clock))
                .build());
    }

    @Transactional(readOnly = true)
    public boolean exists(String action, String entityType, Long entityId) {
        return auditLogRepository.existsByActionAndEntityTypeAndEntityId(action, entityType, entityId);
    }

    /** entityId -> thời điểm ghi sớm nhất của hành động đó, mới nhất trước. */
    @Transactional(readOnly = true)
    public Map<Long, Instant> entityTimesByAction(String action, String entityType) {
        Map<Long, Instant> result = new LinkedHashMap<>();
        for (AuditLog log : auditLogRepository.findByActionAndEntityTypeOrderByIdDesc(action, entityType)) {
            result.merge(log.getEntityId(), log.getCreatedAt(),
                    (a, b) -> a != null && (b == null || a.isBefore(b)) ? a : b);
        }
        return result;
    }
}
