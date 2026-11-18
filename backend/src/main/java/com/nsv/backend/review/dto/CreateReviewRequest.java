package com.nsv.backend.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReviewRequest(
        @NotNull(message = "Vui lòng chọn sản phẩm trong đơn hàng cần đánh giá")
        Long orderItemId,
        @NotNull(message = "Vui lòng chọn số sao")
        @Min(value = 1, message = "Số sao từ 1 đến 5")
        @Max(value = 5, message = "Số sao từ 1 đến 5")
        Integer rating,
        @Size(max = 2000, message = "Bình luận tối đa 2000 ký tự")
        String comment) {
}
