package com.nsv.backend.order.dto;

import jakarta.validation.constraints.NotNull;

public record PaymentSimulateRequest(
        @NotNull(message = "Vui lòng chọn kết quả giao dịch mô phỏng (SUCCESS hoặc FAILED)")
        SimulatedOutcome outcome) {
}
