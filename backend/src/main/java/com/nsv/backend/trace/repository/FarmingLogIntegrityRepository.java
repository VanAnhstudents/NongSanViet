package com.nsv.backend.trace.repository;

import java.util.List;
import java.util.Optional;

public interface FarmingLogIntegrityRepository {

    Optional<Long> findBatchIdByTraceabilityCode(String traceabilityCode);

    /** Nhật ký hiện có của lô, theo id tăng dần. */
    List<LogRecord> findLogsByBatch(Long batchId);

    /** Nhật ký chưa được niêm phong (của mọi lô). */
    List<LogRecord> findUnsealedLogs();

    /** Các con dấu đã ghi cho lô (kể cả của dòng nhật ký đã bị xóa mất). */
    List<SealRecord> findSealsByBatch(Long batchId);

    boolean isSealed(Long logId);

    void saveSeal(Long logId, Long batchId, String mac);
}
