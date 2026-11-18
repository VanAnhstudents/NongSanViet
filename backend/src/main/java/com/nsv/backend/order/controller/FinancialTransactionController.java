package com.nsv.backend.order.controller;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.order.dto.*;
import com.nsv.backend.order.service.FinancialTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FinancialTransactionController {

    private final FinancialTransactionService financialTransactionService;

    @PostMapping("/payments")
    @PreAuthorize("hasAnyRole('BUYER', 'ADMIN')")
    public ResponseEntity<PaymentResponse> processPayment(
            Authentication authentication,
            @Valid @RequestBody ProcessPaymentRequest request
    ) {
        Long actorId = extractUserId(authentication);
        boolean isAdmin = "ADMIN".equals(extractRole(authentication));
        PaymentResponse response = financialTransactionService.processPayment(actorId, isAdmin, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/orders/{orderId}/payments")
    @PreAuthorize("hasAnyRole('BUYER', 'FARMER', 'ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getOrderPayments(
            Authentication authentication,
            @PathVariable Long orderId
    ) {
        Long actorId = extractUserId(authentication);
        String role = extractRole(authentication);
        return ResponseEntity.ok(financialTransactionService.getOrderPayments(actorId, role, orderId));
    }

    @PostMapping("/admin/refunds")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RefundResponse> processRefund(
            Authentication authentication,
            @Valid @RequestBody ProcessRefundRequest request
    ) {
        Long adminId = extractUserId(authentication);
        RefundResponse response = financialTransactionService.processRefund(adminId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/admin/orders/{orderId}/reconciliation")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderFinancialReconciliationResponse> reconcileOrderFinances(
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(financialTransactionService.reconcileOrderFinances(orderId));
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
