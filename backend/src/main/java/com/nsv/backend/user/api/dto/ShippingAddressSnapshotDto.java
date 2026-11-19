package com.nsv.backend.user.api.dto;

public record ShippingAddressSnapshotDto(
    Long addressId,
    String receiverName,
    String receiverPhone,
    String provinceName,
    String addressLine
) {}
