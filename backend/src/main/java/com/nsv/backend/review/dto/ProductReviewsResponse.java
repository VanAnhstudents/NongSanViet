package com.nsv.backend.review.dto;

import java.util.List;

/** Trang sản phẩm: số sao trung bình (làm tròn 1 chữ số), tổng số đánh giá và các đánh giá mới nhất. */
public record ProductReviewsResponse(Long productId, double averageRating, int reviewCount, List<ReviewItem> reviews) {
}
