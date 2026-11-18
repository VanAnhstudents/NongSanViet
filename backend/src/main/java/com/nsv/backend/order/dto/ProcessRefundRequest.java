package com.nsv.backend.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProcessRefundRequest(
    @NotNull(message = "Mã giao dịch thanh toán không được để trống")
    Long paymentId,

    Long complaintId,

    @DecimalMin(value = "0.01", message = "Số tiền hoàn phải lớn hơn 0")
    BigDecimal amount, // Null = hoàn toàn bộ số tiền còn lại của giao dịch

    @NotBlank(message = "Lý do hoàn tiền không được để trống")
    @Size(max = 255, message = "Lý do hoàn tiền tối đa 255 ký tự")
    String reason
) {}
