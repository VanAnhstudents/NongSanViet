package com.nsv.backend.product.spi;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;

/**
 * Cổng giao tiếp do module Sản phẩm định nghĩa để lấy dữ liệu lô hàng từ module Tồn kho.
 *
 * <p>Module Tồn kho là bên triển khai cổng này. Nhờ đó chiều phụ thuộc vẫn là
 * {@code inventory -> product}, đúng với ma trận kiến trúc của Modular Monolith.</p>
 */
public interface ProductBatchPort {

    ProductBatchSnapshot createInitialBatch(
        Long productId,
        BigDecimal unitPrice,
        BigDecimal totalQuantity,
        LocalDate harvestDate,
        String status
    );

    Map<Long, ProductBatchSnapshot> findLatestActiveBatches(
        Collection<Long> productIds,
        Collection<String> activeStatuses
    );
}
