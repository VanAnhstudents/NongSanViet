package com.nsv.backend.order.repository;

/** Các kiểm tra xuyên bảng order_items, batches, products, payments cho luồng hoàn tiền. */
public interface RefundQueryRepository {

    /** Đơn có ít nhất một sản phẩm của Farmer này. */
    boolean orderBelongsToFarmer(Long orderId, Long farmerId);

    /** Đơn còn chứa sản phẩm của Farmer khác. */
    boolean orderHasOtherFarmers(Long orderId, Long farmerId);

    /** Đơn Pre-order: có giao dịch cọc (DEPOSIT) hoặc chứa lô đang ở trạng thái PRE_ORDER. */
    boolean isPreOrder(Long orderId);
}
