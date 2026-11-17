package com.nsv.backend.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateCartItemRequest(
    @NotNull(message = "Số lượng không được để trống")
    @DecimalMin(value = "0.01", message = "Số lượng sản phẩm phải lớn hơn 0")
    BigDecimal quantity
) {}
