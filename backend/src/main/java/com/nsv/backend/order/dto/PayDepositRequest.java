package com.nsv.backend.order.dto;

import jakarta.validation.constraints.Size;

public record PayDepositRequest(
    @Size(max = 100, message = "Mã tham chiếu giao dịch tối đa 100 ký tự")
    String providerRef
) {}
