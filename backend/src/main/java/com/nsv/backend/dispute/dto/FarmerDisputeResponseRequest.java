package com.nsv.backend.dispute.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record FarmerDisputeResponseRequest(
    @NotBlank(message = "Nội dung giải trình không được để trống")
    String responseMessage,

    @Valid
    List<EvidenceItemRequest> evidences
) {}
