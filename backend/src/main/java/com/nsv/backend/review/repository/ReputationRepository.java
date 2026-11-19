package com.nsv.backend.review.repository;

public interface ReputationRepository {

    /** Tính lại từ dữ liệu hiện tại mỗi lần gọi (không lưu cache), nên luôn phản ánh đánh giá/đơn hàng mới nhất. */
    ReputationStats findStatsByFarmer(Long farmerId);
}
