package com.nsv.backend.review.repository;

/** Số liệu thô của một Farmer: tổng hợp từ reviews và orders. */
public record ReputationStats(long reviewCount, long ratingSum, long goodReviewCount, long completedOrders) {
}
