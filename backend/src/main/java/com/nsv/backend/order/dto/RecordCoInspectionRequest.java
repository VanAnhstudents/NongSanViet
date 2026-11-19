package com.nsv.backend.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RecordCoInspectionRequest(
    @NotBlank(message = "Kết quả đồng kiểm không được để trống")
    @Pattern(
        regexp = "PASSED|FAILED",
        message = "Kết quả đồng kiểm chỉ hỗ trợ PASSED (Đạt) hoặc FAILED (Không đạt)"
    )
    String result,

    @Size(max = 1000, message = "Ghi chú đồng kiểm tối đa 1000 ký tự")
    String notes,

    Boolean confirmDelivery // Mặc định true: ghi nhận đồng kiểm và xác nhận "Đã giao" để chốt hoàn tất đơn hàng
) {
    public boolean shouldConfirmDelivery() {
        return confirmDelivery == null || Boolean.TRUE.equals(confirmDelivery);
    }
}
