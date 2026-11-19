package com.nsv.backend.dispute.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

/**
 * Yêu cầu phân xử tranh chấp do quản trị viên gửi.
 * - decision: FULL_REFUND | PARTIAL_REFUND | REJECT_COMPLAINT (không phân biệt hoa/thường).
 * - partialRefundAmount: bắt buộc > 0 khi decision = PARTIAL_REFUND, do tầng service kiểm tra.
 */
public record ResolveDisputeRequest(
    @NotBlank(message = "Quyết định phân xử không được để trống")
    @Pattern(
        regexp = "(?i)\\s*(FULL_REFUND|PARTIAL_REFUND|REJECT_COMPLAINT)\\s*",
        message = "Quyết định phải là FULL_REFUND, PARTIAL_REFUND hoặc REJECT_COMPLAINT"
    )
    String decision,

    @NotBlank(message = "Ghi chú phân xử không được để trống")
    String adminNote,

    BigDecimal partialRefundAmount
) {}
