package com.nsv.backend.user.dto;

public record AddressResponse(
        Long id,
        Long provinceId,
        String provinceName,
        String receiverName,
        String receiverPhone,
        String addressLine) {
}
