package com.nsv.backend.order.dto;

import jakarta.validation.constraints.Size;

public record ConfirmOrderDeliveryRequest(
    @Size(max = 1000, message = "Ghi chú xác nhận nhận hàng tối đa 1000 ký tự")
    String finalNotes
) {}
