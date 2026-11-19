package com.nsv.backend.user.dto;

import com.nsv.backend.user.entity.Farm;
import com.nsv.backend.user.entity.FarmCertificate;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record FarmCertificateResponse(
    Long id,
    Long farmId,
    String farmName,
    Long farmerId,
    String farmerName,
    String certificateType,
    String certificateNumber,
    String issuedBy,
    LocalDate issueDate,
    LocalDate expiryDate,
    String fileUrl,
    String status,
    String rejectionReason,
    Long reviewedBy,
    OffsetDateTime reviewedAt,
    OffsetDateTime createdAt
) {
    public static FarmCertificateResponse from(FarmCertificate cert, Farm farm, String farmerName) {
        return new FarmCertificateResponse(
            cert.getId(),
            cert.getFarmId(),
            farm != null ? farm.getFarmName() : null,
            farm != null ? farm.getUserId() : null,
            farmerName,
            cert.getCertificateType(),
            cert.getCertificateNumber(),
            cert.getIssuedBy(),
            cert.getIssueDate(),
            cert.getExpiryDate(),
            cert.getFileUrl(),
            cert.getStatus(),
            cert.getRejectionReason(),
            cert.getReviewedBy(),
            cert.getReviewedAt(),
            cert.getCreatedAt()
        );
    }
}
