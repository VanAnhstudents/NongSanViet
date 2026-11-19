package com.nsv.backend.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SubmitCertificateRequest(
    @NotBlank(message = "Loại chứng nhận không được để trống")
    @Size(max = 50, message = "Loại chứng nhận tối đa 50 ký tự")
    String certificateType,

    @Size(max = 100, message = "Số hiệu chứng nhận tối đa 100 ký tự")
    String certificateNumber,

    @Size(max = 150, message = "Đơn vị cấp chứng nhận tối đa 150 ký tự")
    String issuedBy,

    LocalDate issueDate,

    LocalDate expiryDate,

    @NotBlank(message = "Đường dẫn tệp/ảnh chứng nhận không được để trống")
    @Size(max = 500, message = "Đường dẫn tệp/ảnh tối đa 500 ký tự")
    String fileUrl
) {}
