package com.nsv.backend.admin.service;

import com.nsv.backend.admin.repository.AdminAuditLogRepository;
import com.nsv.backend.common.audit.entity.AuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AdminAuditLogRepository auditLogRepository;

    /** actorId = null nghĩa là hệ thống tự thực hiện. */
    @Transactional
    public void record(Long actorId, String action, String entityType, Long entityId) {
        auditLogRepository.save(AuditLog.builder()
                .actorId(actorId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .build());
    }

    @Transactional(readOnly = true)
    public boolean exists(String action, String entityType, Long entityId) {
        return auditLogRepository.existsByActionAndEntityTypeAndEntityId(action, entityType, entityId);
    }

    /** entityId -> thời điểm ghi sớm nhất của hành động đó, mới nhất trước. */
    @Transactional(readOnly = true)
    public Map<Long, OffsetDateTime> entityTimesByAction(String action, String entityType) {
        Map<Long, OffsetDateTime> result = new LinkedHashMap<>();
        for (AuditLog log : auditLogRepository.findByActionAndEntityTypeOrderByIdDesc(action, entityType)) {
            result.merge(log.getEntityId(), log.getCreatedAt(),
                    (a, b) -> a != null && (b == null || a.isBefore(b)) ? a : b);
        }
        return result;
    }
}
