package com.nsv.backend.user.dto;

import jakarta.validation.constraints.Size;

/** Thông tin mở rộng của hồ sơ trang trại. Mọi trường đều tùy chọn. */
public record FarmExtendedInfoRequest(
        @Size(max = 500, message = "Đường dẫn chứng nhận tối đa 500 ký tự") String certificateUrl) {
}
