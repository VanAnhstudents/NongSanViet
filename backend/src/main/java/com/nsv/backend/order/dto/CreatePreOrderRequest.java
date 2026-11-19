package com.nsv.backend.order.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreatePreOrderRequest(
    @NotNull(message = "Mã lô hàng đặt trước không được để trống")
    Long batchId,

    @NotNull(message = "Số lượng đặt trước không được để trống")
    @DecimalMin(value = "0.01", message = "Số lượng đặt trước phải lớn hơn 0")
    BigDecimal quantity,

    @NotNull(message = "Địa chỉ nhận hàng không được để trống")
    Long addressId,

    @DecimalMin(value = "0.0", message = "Phí vận chuyển không được âm")
    BigDecimal shippingFee,

    @DecimalMin(value = "0.20", message = "Tỷ lệ đặt cọc tối thiểu theo quy định là 20% (0.20)")
    @DecimalMax(value = "0.30", message = "Tỷ lệ đặt cọc tối đa theo quy định là 30% (0.30)")
    BigDecimal depositRate // Mặc định 0.30 (30%) nếu không truyền
) {}
