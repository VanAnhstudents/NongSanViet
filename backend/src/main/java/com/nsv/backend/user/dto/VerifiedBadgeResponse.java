package com.nsv.backend.user.dto;

import com.nsv.backend.user.entity.FarmCertificate;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record VerifiedBadgeResponse(
    Long certificateId,
    String certificateType,
    String badgeLabel,
    String certificateNumber,
    String issuedBy,
    LocalDate expiryDate,
    OffsetDateTime verifiedAt
) {
    public static VerifiedBadgeResponse from(FarmCertificate cert) {
        return new VerifiedBadgeResponse(
            cert.getId(),
            cert.getCertificateType(),
            resolveBadgeLabel(cert.getCertificateType()),
            cert.getCertificateNumber(),
            cert.getIssuedBy(),
            cert.getExpiryDate(),
            cert.getReviewedAt()
        );
    }

    private static String resolveBadgeLabel(String type) {
        if (type == null) {
            return "Chứng nhận đã xác minh";
        }
        return switch (type.trim().toUpperCase()) {
            case "VIETGAP" -> "Chứng nhận VietGAP";
            case "GLOBALGAP" -> "Chứng nhận GlobalGAP";
            case "ORGANIC" -> "Chứng nhận Hữu cơ";
            case "OCOP" -> "Chứng nhận OCOP";
            case "HACCP" -> "Chứng nhận HACCP";
            default -> "Chứng nhận " + type.trim();
        };
    }
}
