package com.nsv.backend.dispute.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "complaints")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "reason", nullable = false, length = 100)
    private String reason;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "farmer_response", columnDefinition = "TEXT")
    private String farmerResponse;

    @Column(name = "status", nullable = false, length = 25)
    @Builder.Default
    private String status = "OPEN"; // OPEN | IN_REVIEW | RESOLVED_REFUND | RESOLVED_REJECTED

    @Column(name = "admin_decision", length = 30)
    private String adminDecision; // FULL_REFUND | PARTIAL_REFUND | REJECT_COMPLAINT

    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;

    @Column(name = "refund_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal refundAmount = BigDecimal.ZERO;

    @Column(name = "resolved_by")
    private Long resolvedBy;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC, id ASC")
    @Builder.Default
    private List<ComplaintEvidence> evidences = new ArrayList<>();

    public void addEvidence(ComplaintEvidence evidence) {
        evidences.add(evidence);
        evidence.setComplaint(this);
    }
}
