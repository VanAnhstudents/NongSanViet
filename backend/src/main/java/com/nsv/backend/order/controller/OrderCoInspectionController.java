package com.nsv.backend.order.controller;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.order.dto.CoInspectionResponse;
import com.nsv.backend.order.dto.ConfirmOrderDeliveryRequest;
import com.nsv.backend.order.dto.RecordCoInspectionRequest;
import com.nsv.backend.order.service.OrderCoInspectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderCoInspectionController {

    private final OrderCoInspectionService coInspectionService;

    // Buyer ghi nhận kết quả đồng kiểm (PASSED/FAILED, ghi chú) và xác nhận "Đã giao" để hoàn tất đơn hàng
    @PostMapping("/{orderId}/co-inspection")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<CoInspectionResponse> recordCoInspection(
            Authentication authentication,
            @PathVariable Long orderId,
            @Valid @RequestBody RecordCoInspectionRequest request
    ) {
        Long buyerId = extractUserId(authentication);
        return ResponseEntity.ok(coInspectionService.recordCoInspection(buyerId, orderId, request));
    }

    // Buyer xác nhận "Đã giao" đối với phiếu đồng kiểm đã lưu trước đó để chốt hoàn tất đơn hàng
    @PatchMapping("/{orderId}/confirm-delivery")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<CoInspectionResponse> confirmDeliveryAndComplete(
            Authentication authentication,
            @PathVariable Long orderId,
            @Valid @RequestBody(required = false) ConfirmOrderDeliveryRequest request
    ) {
        Long buyerId = extractUserId(authentication);
        return ResponseEntity.ok(coInspectionService.confirmDeliveryAndComplete(buyerId, orderId, request));
    }

    // Xem kết quả đồng kiểm của đơn hàng (Buyer, Farmer của đơn hàng và Admin)
    @GetMapping("/{orderId}/co-inspection")
    @PreAuthorize("hasAnyRole('BUYER', 'FARMER', 'ADMIN')")
    public ResponseEntity<CoInspectionResponse> getCoInspectionByOrder(
            Authentication authentication,
            @PathVariable Long orderId
    ) {
        Long actorId = extractUserId(authentication);
        String role = extractRole(authentication);
        return ResponseEntity.ok(coInspectionService.getCoInspectionByOrder(actorId, role, orderId));
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

    private String extractRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(auth -> auth.startsWith("ROLE_"))
            .map(auth -> auth.substring(5))
            .findFirst()
            .orElse("");
    }
}
