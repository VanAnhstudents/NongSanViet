package com.nsv.backend.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ShortageRequest(
        @NotBlank(message = "Vui lòng nhập lý do thiếu hàng")
        @Size(max = 200, message = "Lý do tối đa 200 ký tự")
        String reason) {
}
