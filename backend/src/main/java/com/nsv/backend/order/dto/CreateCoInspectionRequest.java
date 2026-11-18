package com.nsv.backend.order.dto;

import jakarta.validation.constraints.Size;

public record CreateCoInspectionRequest(
    Boolean checkQuantity,
    Boolean checkQuality,

    @Size(max = 500, message = "Ghi chú đồng kiểm tối đa 500 ký tự")
    String note
) {}
