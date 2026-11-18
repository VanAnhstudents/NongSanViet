package com.nsv.backend.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Sửa lô: không đổi sản phẩm. */
public record BatchUpdateRequest(
        @NotNull(message = "Vui lòng nhập sản lượng dự kiến")
        @DecimalMin(value = "0.01", message = "Sản lượng phải lớn hơn 0")
        @Digits(integer = 8, fraction = 2, message = "Sản lượng tối đa 8 chữ số phần nguyên và 2 chữ số thập phân")
        BigDecimal totalQuantity,

        @NotNull(message = "Vui lòng nhập giá bán")
        @DecimalMin(value = "0.01", message = "Giá bán phải lớn hơn 0")
        @Digits(integer = 13, fraction = 2, message = "Giá bán tối đa 13 chữ số phần nguyên và 2 chữ số thập phân")
        BigDecimal unitPrice,

        @NotNull(message = "Vui lòng nhập ngày thu hoạch dự kiến")
        LocalDate harvestDate) {
}
