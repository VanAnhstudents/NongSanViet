package com.nsv.backend.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.List;

public record CheckoutRequest(
    @NotNull(message = "Địa chỉ nhận hàng không được để trống")
    Long addressId,

    @NotBlank(message = "Phương thức thanh toán không được để trống")
    @Pattern(regexp = "COD|ONLINE", message = "Phương thức thanh toán chỉ hỗ trợ COD hoặc ONLINE")
    String paymentMethod,

    @DecimalMin(value = "0.0", message = "Phí vận chuyển không được âm")
    BigDecimal shippingFee,

    List<Long> cartItemIds // Null hoặc rỗng: đặt toàn bộ sản phẩm trong giỏ hàng
) {}
