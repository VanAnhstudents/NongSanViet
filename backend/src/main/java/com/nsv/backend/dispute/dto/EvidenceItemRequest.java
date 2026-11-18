package com.nsv.backend.dispute.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EvidenceItemRequest(
    @NotBlank(message = "Loại bằng chứng không được để trống")
    @Pattern(regexp = "IMAGE|VIDEO", message = "Loại bằng chứng chỉ hỗ trợ IMAGE hoặc VIDEO")
    String mediaType,

    @NotBlank(message = "Đường dẫn bằng chứng không được để trống")
    @Size(max = 500, message = "Đường dẫn tối đa 500 ký tự")
    String mediaUrl,

    @Size(max = 255, message = "Mô tả bằng chứng tối đa 255 ký tự")
    String caption
) {}
