package com.nsv.backend.order.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProcessPaymentRequest(
    @NotNull(message = "Mã đơn hàng không được để trống")
    Long orderId,

    @NotBlank(message = "Loại thanh toán không được để trống")
    @Pattern(
        regexp = "FULL|DEPOSIT|REMAINING",
        message = "Loại thanh toán chỉ hỗ trợ FULL, DEPOSIT hoặc REMAINING"
    )
    String paymentType,

    @DecimalMin(value = "0.20", message = "Tỷ lệ đặt cọc tối thiểu là 20% (0.20)")
    @DecimalMax(value = "0.30", message = "Tỷ lệ đặt cọc tối đa là 30% (0.30)")
    BigDecimal depositRate, // Mặc định 0.30 (30%) nếu paymentType = DEPOSIT

    @Size(max = 100, message = "Mã tham chiếu giao dịch tối đa 100 ký tự")
    String providerRef
) {}
