package com.nsv.backend.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateOrderStatusRequest(
    @NotBlank(message = "Trạng thái cập nhật không được để trống")
    @Pattern(
        regexp = "CONFIRMED|SHIPPING|DELIVERED",
        message = "Trạng thái cập nhật của Farmer chỉ hợp lệ với: CONFIRMED, SHIPPING, DELIVERED"
    )
    String status
) {}
