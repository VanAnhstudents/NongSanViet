package com.nsv.backend.common.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "type", nullable = false, length = 40)
    @Builder.Default
    private String type = "SYSTEM"; // NEW_ORDER | ORDER_STATUS_CHANGED | NEW_MESSAGE | DISPUTE_UPDATE | SYSTEM

    @Column(name = "title", nullable = false, length = 150)
    @Builder.Default
    private String title = "Thông báo hệ thống";

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "reference_type", length = 40)
    private String referenceType; // ORDER | MESSAGE | COMPLAINT | CERTIFICATE

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "read_at")
    private OffsetDateTime readAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
