package com.nsv.backend.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "farms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Farm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "province_id", nullable = false)
    private Long provinceId;

    @Column(name = "main_category_id", nullable = false)
    private Long mainCategoryId;

    @Column(name = "farm_name", nullable = false, length = 150)
    private String farmName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "address_line", nullable = false, length = 255)
    private String addressLine;

    @Column(name = "certificate_status", nullable = false, length = 20)
    @Builder.Default
    private String certificateStatus = "NONE"; // NONE | PENDING | VERIFIED | REJECTED

    @Column(name = "certificate_url", length = 500)
    private String certificateUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
