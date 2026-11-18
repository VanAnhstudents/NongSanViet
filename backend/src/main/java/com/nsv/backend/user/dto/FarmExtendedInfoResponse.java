package com.nsv.backend.user.dto;

public record FarmExtendedInfoResponse(
        Long farmId,
        String farmName,
        String certificateUrl,
        String certificateStatus) {
}
