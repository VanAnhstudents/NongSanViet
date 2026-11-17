package com.nsv.backend.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ComboItemRequest(
    @NotNull(message = "Mã sản phẩm không được để trống")
    Long productId,

    @NotNull(message = "Số lượng sản phẩm không được để trống")
    @DecimalMin(value = "0.01", message = "Số lượng sản phẩm phải lớn hơn 0")
    BigDecimal quantity
) {}
