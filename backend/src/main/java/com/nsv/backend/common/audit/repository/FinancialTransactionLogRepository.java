package com.nsv.backend.common.audit.repository;

import com.nsv.backend.common.audit.entity.FinancialTransactionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface FinancialTransactionLogRepository extends JpaRepository<FinancialTransactionLog, Long> {

    List<FinancialTransactionLog> findAllByOrderIdOrderByCreatedAtDescIdDesc(Long orderId);

    List<FinancialTransactionLog> findAllByUserIdOrderByCreatedAtDescIdDesc(Long userId);

    @Query("""
        SELECT f FROM FinancialTransactionLog f
        WHERE (:orderId IS NULL OR f.orderId = :orderId)
          AND (:userId IS NULL OR f.userId = :userId)
          AND (:transactionType IS NULL OR f.transactionType = :transactionType)
          AND (:status IS NULL OR f.status = :status)
          AND (:fromTime IS NULL OR f.createdAt >= :fromTime)
          AND (:toTime IS NULL OR f.createdAt <= :toTime)
        ORDER BY f.createdAt DESC, f.id DESC
        """)
    Page<FinancialTransactionLog> searchFinancialLogs(
        @Param("orderId") Long orderId,
        @Param("userId") Long userId,
        @Param("transactionType") String transactionType,
        @Param("status") String status,
        @Param("fromTime") OffsetDateTime fromTime,
        @Param("toTime") OffsetDateTime toTime,
        Pageable pageable
    );
}
