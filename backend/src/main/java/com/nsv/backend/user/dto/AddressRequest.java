package com.nsv.backend.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotNull(message = "Vui lòng chọn tỉnh/thành phố")
        Long provinceId,
        @NotBlank(message = "Tên người nhận không được để trống")
        @Size(max = 100, message = "Tên người nhận tối đa 100 ký tự")
        String receiverName,
        @NotBlank(message = "Số điện thoại người nhận không được để trống")
        @Pattern(regexp = "^(0|\\+84)\\d{9}$",
                message = "Số điện thoại không hợp lệ (ví dụ 0912345678 hoặc +84912345678)")
        String receiverPhone,
        @NotBlank(message = "Địa chỉ cụ thể không được để trống")
        @Size(max = 255, message = "Địa chỉ cụ thể tối đa 255 ký tự")
        String addressLine) {
}
