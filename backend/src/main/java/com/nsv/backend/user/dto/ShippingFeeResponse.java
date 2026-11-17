package com.nsv.backend.user.dto;

import java.math.BigDecimal;

public record ShippingFeeResponse(
        Long addressId,
        Long farmerId,
        Long fromProvinceId,
        Long toProvinceId,
        boolean sameProvince,
        BigDecimal shippingFee) {
}
