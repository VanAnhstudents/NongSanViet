package com.nsv.backend.order.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "order_co_inspections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCoInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "result", nullable = false, length = 20)
    private String result; // PASSED | FAILED

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_locked", nullable = false)
    @Builder.Default
    private Boolean locked = false;

    @Column(name = "confirmed_at")
    private OffsetDateTime confirmedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public boolean isPassed() {
        return "PASSED".equalsIgnoreCase(this.result);
    }

    public boolean isLocked() {
        return Boolean.TRUE.equals(this.locked);
    }
}
