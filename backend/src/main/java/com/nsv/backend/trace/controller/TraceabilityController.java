package com.nsv.backend.trace.controller;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.trace.dto.CreateFarmingLogRequest;
import com.nsv.backend.trace.dto.FarmingLogResponse;
import com.nsv.backend.trace.dto.TraceabilityResponse;
import com.nsv.backend.trace.service.TraceabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TraceabilityController {

    private final TraceabilityService traceabilityService;

    // Public: Buyer quét mã QR (UUID) trên bao bì sản phẩm để xem nguồn gốc & nhật ký canh tác
    @GetMapping("/trace/{traceabilityCode}")
    public ResponseEntity<TraceabilityResponse> scanQrCode(@PathVariable String traceabilityCode) {
        return ResponseEntity.ok(traceabilityService.getTraceabilityByCode(traceabilityCode));
    }

    // Public: Lấy thông tin mã QR duy nhất và dữ liệu truy xuất gắn với một lô hàng cụ thể
    @GetMapping("/batches/{batchId}/trace")
    public ResponseEntity<TraceabilityResponse> getBatchTraceability(@PathVariable Long batchId) {
        return ResponseEntity.ok(traceabilityService.getTraceabilityByBatchId(batchId));
    }

    // Farmer: Bổ sung bản ghi nhật ký canh tác cho lô hàng của trang trại mình
    @PostMapping("/farmer/batches/{batchId}/farming-logs")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<FarmingLogResponse> addFarmingLog(
            Authentication authentication,
            @PathVariable Long batchId,
            @Valid @RequestBody CreateFarmingLogRequest request
    ) {
        Long farmerId = extractUserId(authentication);
        FarmingLogResponse response = traceabilityService.addFarmingLog(farmerId, batchId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Chưa xác thực người dùng");
        }
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException ex) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN_SUBJECT", "Thông tin định danh không hợp lệ");
        }
    }
}
