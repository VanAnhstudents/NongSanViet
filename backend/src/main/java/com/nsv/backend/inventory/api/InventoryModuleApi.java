package com.nsv.backend.inventory.api;

import com.nsv.backend.inventory.api.dto.BatchReservationItemDto;
import com.nsv.backend.inventory.api.dto.BatchSnapshotDto;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface InventoryModuleApi {
    Optional<BatchSnapshotDto> findBatchById(Long batchId);
    Optional<BatchSnapshotDto> findBatchByTraceabilityCode(UUID traceabilityCode);
    Map<Long, BatchSnapshotDto> findBatchesByIds(Collection<Long> batchIds);
    Map<Long, BatchSnapshotDto> reserveStockForOrder(List<BatchReservationItemDto> items);
}
