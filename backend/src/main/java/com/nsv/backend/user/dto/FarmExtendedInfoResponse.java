package com.nsv.backend.user.dto;

import com.nsv.backend.user.entity.CertificateStatus;

public record FarmExtendedInfoResponse(
        Long farmId,
        String farmName,
        String certificateUrl,
        CertificateStatus certificateStatus) {
}
