package com.nsv.backend.inventory.service;

import com.nsv.backend.inventory.entity.Batch;
import com.nsv.backend.inventory.repository.BatchRepository;
import com.nsv.backend.product.spi.ProductBatchPort;
import com.nsv.backend.product.spi.ProductBatchSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Triển khai cổng {@link ProductBatchPort} của module Sản phẩm.
 * Module Tồn kho sở hữu entity/repository lô hàng nên đây là nơi duy nhất truy cập trực tiếp.
 */
@Service
@RequiredArgsConstructor
public class ProductBatchPortAdapter implements ProductBatchPort {

    private final BatchRepository batchRepository;

    @Override
    public ProductBatchSnapshot createInitialBatch(
            Long productId,
            BigDecimal unitPrice,
            BigDecimal totalQuantity,
            LocalDate harvestDate,
            String status) {
        Batch batch = Batch.builder()
            .productId(productId)
            .traceabilityCode(UUID.randomUUID())
            .harvestDate(harvestDate)
            .unitPrice(unitPrice)
            .totalQuantity(totalQuantity)
            .remainingQuantity(totalQuantity)
            .status(status)
            .build();
        return toSnapshot(batchRepository.save(batch));
    }

    @Override
    public Map<Long, ProductBatchSnapshot> findLatestActiveBatches(
            Collection<Long> productIds,
            Collection<String> activeStatuses) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, ProductBatchSnapshot> result = new HashMap<>();
        for (Batch batch : batchRepository.findAllByProductIdInAndStatusInOrderByCreatedAtDesc(
                productIds, activeStatuses)) {
            result.putIfAbsent(batch.getProductId(), toSnapshot(batch));
        }
        return result;
    }

    private ProductBatchSnapshot toSnapshot(Batch batch) {
        return new ProductBatchSnapshot(
            batch.getId(),
            batch.getProductId(),
            batch.getTraceabilityCode(),
            batch.getHarvestDate(),
            batch.getUnitPrice(),
            batch.getTotalQuantity(),
            batch.getRemainingQuantity(),
            batch.getStatus()
        );
    }
}
