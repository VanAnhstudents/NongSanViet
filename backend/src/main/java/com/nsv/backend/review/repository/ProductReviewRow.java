package com.nsv.backend.review.repository;

import java.time.Instant;

/** Một đánh giá của sản phẩm kèm tên người đánh giá. */
public record ProductReviewRow(Long id, int rating, String comment, Instant createdAt, String reviewerName) {
}
