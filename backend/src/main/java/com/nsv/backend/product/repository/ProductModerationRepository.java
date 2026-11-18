package com.nsv.backend.product.repository;

/** Thao tác kiểm duyệt sản phẩm. Bảng products không có cột trạng thái nên "gỡ" = đóng mọi lô của sản phẩm. */
public interface ProductModerationRepository {

    boolean productExists(Long productId);

    /** Chuyển mọi lô chưa CLOSED của sản phẩm sang CLOSED; trả về số lô đã đổi. */
    int closeAllBatches(Long productId);
}
