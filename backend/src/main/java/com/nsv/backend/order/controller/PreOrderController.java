package com.nsv.backend.order.controller;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.order.dto.CreatePreOrderRequest;
import com.nsv.backend.order.dto.PayDepositRequest;
import com.nsv.backend.order.dto.PreOrderResponse;
import com.nsv.backend.order.service.PreOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@PreAuthorize("hasRole('BUYER')")
@RequiredArgsConstructor
public class PreOrderController {

    private final PreOrderService preOrderService;

    // Buyer tạo đơn đặt trước (Pre-order) cho sản phẩm có gắn lịch thu hoạch và nhận yêu cầu đặt cọc
    @PostMapping("/pre-order")
    public ResponseEntity<PreOrderResponse> createPreOrder(
            Authentication authentication,
            @Valid @RequestBody CreatePreOrderRequest request
    ) {
        Long buyerId = extractUserId(authentication);
        PreOrderResponse response = preOrderService.createPreOrder(buyerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Buyer thanh toán tiền đặt cọc theo tỷ lệ quy định để xác nhận giữ chỗ hàng
    @PostMapping("/{orderId}/deposit")
    public ResponseEntity<PreOrderResponse> payPreOrderDeposit(
            Authentication authentication,
            @PathVariable Long orderId,
            @Valid @RequestBody(required = false) PayDepositRequest request
    ) {
        Long buyerId = extractUserId(authentication);
        return ResponseEntity.ok(preOrderService.payPreOrderDeposit(buyerId, orderId, request));
    }

    // Buyer xem danh sách đơn đặt trước với trạng thái riêng biệt
    @GetMapping("/pre-orders")
    public ResponseEntity<List<PreOrderResponse>> getBuyerPreOrders(Authentication authentication) {
        Long buyerId = extractUserId(authentication);
        return ResponseEntity.ok(preOrderService.getBuyerPreOrders(buyerId));
    }

    // Buyer xem chi tiết một đơn đặt trước
    @GetMapping("/pre-orders/{orderId}")
    public ResponseEntity<PreOrderResponse> getBuyerPreOrderDetail(
            Authentication authentication,
            @PathVariable Long orderId
    ) {
        Long buyerId = extractUserId(authentication);
        return ResponseEntity.ok(preOrderService.getBuyerPreOrderDetail(buyerId, orderId));
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
