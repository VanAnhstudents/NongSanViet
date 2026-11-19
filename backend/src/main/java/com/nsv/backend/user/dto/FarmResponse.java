package com.nsv.backend.user.dto;

import com.nsv.backend.user.entity.Farm;

public record FarmResponse(
    Long id,
    Long userId,
    String farmName,
    Long provinceId,
    String addressLine,
    Long mainCategoryId,
    String certificateStatus
) {
    public static FarmResponse from(Farm farm) {
        return new FarmResponse(
            farm.getId(),
            farm.getUserId(),
            farm.getFarmName(),
            farm.getProvinceId(),
            farm.getAddressLine(),
            farm.getMainCategoryId(),
            farm.getCertificateStatus()
        );
    }
}