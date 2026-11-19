package com.nsv.backend.inventory.entity;

import com.nsv.backend.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "batches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    /** Quan hệ chỉ đọc phục vụ truy vấn/response; ghi dữ liệu qua productId. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    @Column(name = "traceability_code", nullable = false, unique = true)
    private UUID traceabilityCode;

    @Column(name = "harvest_date", nullable = false)
    private LocalDate harvestDate;

    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalQuantity;

    @Column(name = "remaining_quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal remainingQuantity;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // PRE_ORDER | AVAILABLE | SOLD_OUT | CLOSED

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
