package com.nsv.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ForgotPasswordRequest(
        @NotBlank(message = "Số điện thoại không được để trống")
        @Pattern(regexp = "^(0|\\+84)\\d{9}$",
                message = "Số điện thoại không hợp lệ (ví dụ 0912345678 hoặc +84912345678)")
        String phone) {
}
