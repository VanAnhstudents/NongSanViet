package com.nsv.backend.trace.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "farming_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "activity", nullable = false, length = 100)
    private String activity;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "image_url", length = 500)
    private String imageUrl;
}
