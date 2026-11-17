package com.nsv.backend.order.controller;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.order.dto.OrderResponse;
import com.nsv.backend.order.dto.UpdateOrderStatusRequest;
import com.nsv.backend.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/farmer/orders")
@PreAuthorize("hasRole('FARMER')")
@RequiredArgsConstructor
public class FarmerOrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getFarmerOrders(
            Authentication authentication,
            @RequestParam(required = false) String status
    ) {
        Long farmerId = extractUserId(authentication);
        return ResponseEntity.ok(orderService.getFarmerOrders(farmerId, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getFarmerOrderDetail(
            Authentication authentication,
            @PathVariable Long id
    ) {
        Long farmerId = extractUserId(authentication);
        return ResponseEntity.ok(orderService.getFarmerOrderDetail(farmerId, id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        Long farmerId = extractUserId(authentication);
        return ResponseEntity.ok(orderService.updateOrderStatusByFarmer(farmerId, id, request));
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
