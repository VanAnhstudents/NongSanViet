package com.nsv.backend.order.dto;

public record OrderAddressResponse(
    String receiverName,
    String receiverPhone,
    String provinceName,
    String addressLine
) {}
