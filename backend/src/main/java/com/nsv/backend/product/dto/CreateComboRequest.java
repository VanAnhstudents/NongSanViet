package com.nsv.backend.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record CreateComboRequest(
    @NotBlank(message = "Tên combo không được để trống")
    @Size(max = 150, message = "Tên combo tối đa 150 ký tự")
    String name,

    String description,

    @NotNull(message = "Giá combo không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Giá combo không được âm")
    BigDecimal comboPrice,

    @NotNull(message = "Danh sách sản phẩm thành phần không được để trống")
    @Size(min = 2, message = "Combo phải bao gồm ít nhất 2 sản phẩm")
    @Valid
    List<ComboItemRequest> items
) {}
