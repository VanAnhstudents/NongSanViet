package com.nsv.backend.trace.repository;

import com.nsv.backend.trace.entity.FarmingLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FarmingLogRepository extends JpaRepository<FarmingLog, Long> {
    List<FarmingLog> findAllByBatchIdOrderByLogDateAscIdAsc(Long batchId);
}
