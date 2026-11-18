package com.nsv.backend.complaint.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Ánh xạ bảng complaints (bản tối thiểu cho PBI-30).
 * Nếu PBI tạo khiếu nại đã có entity Complaint thì xóa file này và dùng bản đó.
 */
@Entity
@Table(name = "complaints")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "farmer_response", columnDefinition = "TEXT")
    private String farmerResponse;

    @Column(name = "admin_decision", columnDefinition = "TEXT")
    private String adminDecision;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
