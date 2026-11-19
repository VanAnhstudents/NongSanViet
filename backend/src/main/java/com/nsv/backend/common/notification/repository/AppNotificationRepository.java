package com.nsv.backend.common.notification.repository;

import com.nsv.backend.common.notification.entity.AppNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface AppNotificationRepository extends JpaRepository<AppNotification, Long> {

    List<AppNotification> findAllByUserIdOrderByCreatedAtDescIdDesc(Long userId);

    List<AppNotification> findAllByUserIdAndIsReadFalseOrderByCreatedAtDescIdDesc(Long userId);

    Optional<AppNotification> findByIdAndUserId(Long id, Long userId);

    long countByUserIdAndIsReadFalse(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE AppNotification n
        SET n.isRead = true,
            n.readAt = :readAt
        WHERE n.userId = :userId
          AND n.isRead = false
        """)
    int markAllAsReadByUserId(
        @Param("userId") Long userId,
        @Param("readAt") OffsetDateTime readAt
    );
}
