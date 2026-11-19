package com.nsv.backend.review.dto;

/**
 * Chỉ số uy tín của Farmer.
 * averageRating: sao trung bình (1 chữ số thập phân, 0 nếu chưa có đánh giá);
 * goodReviewRate: % đánh giá từ 4 sao trở lên; completedOrders: số đơn đã giao/hoàn tất có hàng của Farmer.
 */
public record FarmerReputationResponse(
        Long farmerId,
        String farmerName,
        double averageRating,
        int reviewCount,
        double goodReviewRate,
        int completedOrders) {
}
