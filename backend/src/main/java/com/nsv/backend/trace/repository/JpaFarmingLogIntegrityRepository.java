package com.nsv.backend.trace.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Truy vấn SQL thuần trên farming_logs, batches và audit_logs.
 * Con dấu lưu trong audit_logs: action = "SEAL:&lt;batchId&gt;:&lt;mac&gt;" (&lt;= 50 ký tự), entity_type = FARMING_LOG, entity_id = id dòng nhật ký.
 */
@Repository
public class JpaFarmingLogIntegrityRepository implements FarmingLogIntegrityRepository {

    private static final String LOG_COLUMNS =
            "select fl.id, fl.batch_id, b.traceability_code, fl.log_date, fl.activity, fl.note, fl.image_url "
                    + "from farming_logs fl join batches b on b.id = fl.batch_id ";
    private static final Pattern SEAL_ACTION = Pattern.compile("^SEAL:(\\d+):(.+)$");

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Long> findBatchIdByTraceabilityCode(String traceabilityCode) {
        List<?> rows = entityManager.createNativeQuery(
                        "select b.id from batches b where b.traceability_code = cast(:code as uuid)")
                .setParameter("code", traceabilityCode)
                .getResultList();
        return rows.isEmpty() ? Optional.empty() : Optional.of(((Number) rows.get(0)).longValue());
    }

    @Override
    public List<LogRecord> findLogsByBatch(Long batchId) {
        List<?> rows = entityManager.createNativeQuery(
                        LOG_COLUMNS + "where fl.batch_id = :batchId order by fl.id")
                .setParameter("batchId", batchId)
                .getResultList();
        return rows.stream().map(r -> toLog((Object[]) r)).toList();
    }

    @Override
    public List<LogRecord> findUnsealedLogs() {
        List<?> rows = entityManager.createNativeQuery(
                        LOG_COLUMNS + "where not exists (select 1 from audit_logs a "
                                + "where a.entity_type = 'FARMING_LOG' and a.entity_id = fl.id "
                                + "and a.action like 'SEAL:%') order by fl.id")
                .getResultList();
        return rows.stream().map(r -> toLog((Object[]) r)).toList();
    }

    @Override
    public List<SealRecord> findSealsByBatch(Long batchId) {
        List<?> rows = entityManager.createNativeQuery(
                        "select a.entity_id, a.action from audit_logs a "
                                + "where a.entity_type = 'FARMING_LOG' and a.action like concat('SEAL:', :batchId, ':%') "
                                + "order by a.id")
                .setParameter("batchId", batchId)
                .getResultList();
        return rows.stream().map(r -> {
            Object[] a = (Object[]) r;
            Matcher m = SEAL_ACTION.matcher(String.valueOf(a[1]));
            return m.matches()
                    ? new SealRecord(((Number) a[0]).longValue(), Long.parseLong(m.group(1)), m.group(2))
                    : null;
        }).filter(s -> s != null).toList();
    }

    @Override
    public boolean isSealed(Long logId) {
        Number count = (Number) entityManager.createNativeQuery(
                        "select count(*) from audit_logs a where a.entity_type = 'FARMING_LOG' "
                                + "and a.entity_id = :id and a.action like 'SEAL:%'")
                .setParameter("id", logId)
                .getSingleResult();
        return count.longValue() > 0;
    }

    @Override
    public void saveSeal(Long logId, Long batchId, String mac) {
        entityManager.createNativeQuery(
                        "insert into audit_logs (actor_id, action, entity_type, entity_id, created_at) "
                                + "values (null, :action, 'FARMING_LOG', :id, now())")
                .setParameter("action", "SEAL:" + batchId + ":" + mac)
                .setParameter("id", logId)
                .executeUpdate();
    }

    private static LogRecord toLog(Object[] a) {
        return new LogRecord(((Number) a[0]).longValue(), ((Number) a[1]).longValue(), String.valueOf(a[2]),
                toLocalDate(a[3]), (String) a[4], (String) a[5], (String) a[6]);
    }

    private static LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate d) {
            return d;
        }
        if (value instanceof java.sql.Date d) {
            return d.toLocalDate();
        }
        if (value instanceof Timestamp t) {
            return t.toLocalDateTime().toLocalDate();
        }
        if (value instanceof LocalDateTime t) {
            return t.toLocalDate();
        }
        return value == null ? null : LocalDate.parse(String.valueOf(value));
    }
}
