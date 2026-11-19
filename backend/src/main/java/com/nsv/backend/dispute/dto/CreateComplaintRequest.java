package com.nsv.backend.dispute.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateComplaintRequest(
    @NotBlank(message = "Lý do khiếu nại không được để trống")
    @Size(max = 100, message = "Lý do khiếu nại tối đa 100 ký tự")
    String reason,

    @NotBlank(message = "Mô tả chi tiết khiếu nại không được để trống")
    String description,

    @NotEmpty(message = "Cần cung cấp ít nhất 1 ảnh hoặc video bằng chứng")
    @Valid
    List<EvidenceItemRequest> evidences
) {}
