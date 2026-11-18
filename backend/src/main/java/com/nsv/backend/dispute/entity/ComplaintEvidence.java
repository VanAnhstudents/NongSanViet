package com.nsv.backend.dispute.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "complaint_evidences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @Column(name = "uploaded_by", nullable = false)
    private Long uploadedBy;

    @Column(name = "uploader_role", nullable = false, length = 20)
    private String uploaderRole; // BUYER | FARMER

    @Column(name = "media_type", nullable = false, length = 20)
    private String mediaType; // IMAGE | VIDEO

    @Column(name = "media_url", nullable = false, length = 500)
    private String mediaUrl;

    @Column(name = "caption", length = 255)
    private String caption;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
