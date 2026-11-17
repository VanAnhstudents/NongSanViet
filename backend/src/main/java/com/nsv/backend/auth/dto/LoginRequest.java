package com.nsv.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Vui lòng nhập email hoặc số điện thoại")
        String identifier,

        @NotBlank(message = "Vui lòng nhập mật khẩu")
        String password) {
}
