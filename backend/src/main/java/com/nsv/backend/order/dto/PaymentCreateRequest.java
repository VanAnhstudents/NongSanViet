package com.nsv.backend.order.dto;

import com.nsv.backend.order.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record PaymentCreateRequest(
        @NotNull(message = "Vui lòng chọn hình thức thanh toán (COD hoặc ONLINE)")
        PaymentMethod paymentMethod) {
}
