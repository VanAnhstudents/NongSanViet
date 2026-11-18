package com.nsv.backend.review.repository;

import java.util.List;
import java.util.Optional;

/** Truy vấn đọc xuyên bảng cho đánh giá (order_items, orders, batches, users). */
public interface ReviewQueryRepository {

    /** Trạng thái đơn hàng chứa order_item, chỉ khi đơn thuộc Buyer này. */
    Optional<String> findOrderStatusForBuyer(Long orderItemId, Long buyerId);

    boolean productExists(Long productId);

    /** Mọi đánh giá của sản phẩm, mới nhất trước. */
    List<ProductReviewRow> findByProduct(Long productId);
}
