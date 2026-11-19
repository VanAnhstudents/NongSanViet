package com.nsv.backend.trace.service;

import com.nsv.backend.trace.dto.CreateFarmingLogRequest;
import com.nsv.backend.trace.dto.FarmingLogResponse;
import com.nsv.backend.trace.dto.TraceabilityResponse;

public interface TraceabilityService {
    TraceabilityResponse getTraceabilityByCode(String traceabilityCode);
    TraceabilityResponse getTraceabilityByBatchId(Long batchId);
    FarmingLogResponse addFarmingLog(Long farmerId, Long batchId, CreateFarmingLogRequest request);
}
