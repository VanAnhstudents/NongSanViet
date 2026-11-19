package com.nsv.backend.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ReviewCertificateRequest(
    @NotBlank(message = "Quyết định xét duyệt không được để trống")
    @Pattern(
        regexp = "APPROVED|REJECTED",
        message = "Quyết định xét duyệt chỉ hỗ trợ APPROVED hoặc REJECTED"
    )
    String decision,

    @Size(max = 500, message = "Lý do từ chối tối đa 500 ký tự")
    String rejectionReason
) {}
