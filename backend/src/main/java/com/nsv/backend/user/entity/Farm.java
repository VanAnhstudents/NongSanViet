package com.nsv.backend.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ánh xạ bảng farms (bản tối thiểu cho PBI-11).
 * Nếu PBI-02 đã có entity Farm thì xóa file này và dùng bản của PBI-02.
 */
@Entity
@Table(name = "farms")
@Getter
@Setter
@NoArgsConstructor
public class Farm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "province_id", nullable = false)
    private Long provinceId;

    @Column(name = "farm_name", nullable = false, length = 150)
    private String farmName;

    @Column(name = "address_line", nullable = false, length = 255)
    private String addressLine;

    @Column(name = "certificate_url", length = 500)
    private String certificateUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "certificate_status", nullable = false, length = 20)
    private CertificateStatus certificateStatus = CertificateStatus.NONE;
}
