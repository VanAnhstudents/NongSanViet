package com.nsv.backend.common.audit.controller;

import com.nsv.backend.common.audit.dto.AuditLogResponse;
import com.nsv.backend.common.audit.dto.FinancialTransactionLogResponse;
import com.nsv.backend.common.audit.dto.PagedLogResponse;
import com.nsv.backend.common.audit.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminAuditLogController {

    private final AuditLogService auditLogService;

    // 1. Admin tra cứu toàn bộ log thao tác quản trị (khóa tài khoản, hoàn tiền, gỡ sản phẩm...)
    @GetMapping("/audit-logs")
    public ResponseEntity<PagedLogResponse<AuditLogResponse>> getAuditLogs(
            @RequestParam(required = false) Long actorId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long targetUserId,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
            auditLogService.searchAdminAuditLogs(
                actorId, category, action, entityType, targetUserId, orderId, from, to, page, size
            )
        );
    }

    // 2. Admin tra cứu log giao dịch tài chính đa điều kiện (theo đơn hàng, người dùng, loại giao dịch, thời gian)
    @GetMapping("/financial-logs")
    public ResponseEntity<PagedLogResponse<FinancialTransactionLogResponse>> getFinancialTransactionLogs(
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String transactionType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
            auditLogService.searchFinancialLogs(
                orderId, userId, transactionType, status, from, to, page, size
            )
        );
    }

    // 3. Tra cứu nhanh log giao dịch tài chính theo một đơn hàng cụ thể
    @GetMapping("/orders/{orderId}/financial-logs")
    public ResponseEntity<List<FinancialTransactionLogResponse>> getFinancialLogsByOrder(
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(auditLogService.getFinancialLogsByOrderId(orderId));
    }

    // 4. Tra cứu nhanh log giao dịch tài chính theo một người dùng cụ thể
    @GetMapping("/users/{userId}/financial-logs")
    public ResponseEntity<List<FinancialTransactionLogResponse>> getFinancialLogsByUser(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(auditLogService.getFinancialLogsByUserId(userId));
    }
}
