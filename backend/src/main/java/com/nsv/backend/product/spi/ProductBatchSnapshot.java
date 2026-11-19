package com.nsv.backend.product.spi;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Ảnh chụp dữ liệu lô hàng mà module Sản phẩm cần để hiển thị thông tin mở bán.
 * Đây là DTO của cổng giao tiếp do module Sản phẩm định nghĩa, không phải entity nội bộ
 * của module Tồn kho.
 */
public record ProductBatchSnapshot(
    Long id,
    Long productId,
    UUID traceabilityCode,
    LocalDate harvestDate,
    BigDecimal unitPrice,
    BigDecimal totalQuantity,
    BigDecimal remainingQuantity,
    String status
) {
}
