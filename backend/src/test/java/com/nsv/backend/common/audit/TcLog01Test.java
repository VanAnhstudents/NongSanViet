package com.nsv.backend.common.audit;

import com.nsv.backend.common.audit.dto.AuditLogResponse;
import com.nsv.backend.common.audit.dto.FinancialTransactionLogResponse;
import com.nsv.backend.common.audit.dto.PagedLogResponse;
import com.nsv.backend.common.audit.entity.AuditLog;
import com.nsv.backend.common.audit.entity.FinancialTransactionLog;
import com.nsv.backend.common.audit.repository.AuditLogRepository;
import com.nsv.backend.common.audit.repository.FinancialTransactionLogRepository;
import com.nsv.backend.common.audit.service.AuditLogServiceImpl;
import com.nsv.backend.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TcLog01Test {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private FinancialTransactionLogRepository financialLogRepository;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    @Test
    @DisplayName("TC-LOG-01 (1a): Ghi log thao tác quản trị khóa tài khoản kèm người thực hiện, đối tượng và thời gian")
    void logUserAccountLockChange_RecordsActorTargetUserAndTimestamp() {
        Long adminId = 1L;
        Long lockedFarmerId = 15L;
        OffsetDateTime recordedTime = OffsetDateTime.now();

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(inv -> {
            AuditLog log = inv.getArgument(0);
            log.setId(5001L);
            log.setCreatedAt(recordedTime);
            return log;
        });

        AuditLogResponse response = auditLogService.logUserAccountLockChange(
            adminId,
            lockedFarmerId,
            true,
            "Gian lận nguồn gốc sản phẩm nhiều lần"
        );

        assertEquals(5001L, response.id());
        assertEquals(adminId, response.actorId());
        assertEquals("ADMIN", response.actorRole());
        assertEquals("ADMIN_ACTION", response.category());
        assertEquals("LOCK_USER_ACCOUNT", response.action());
        assertEquals("users", response.entityType());
        assertEquals(lockedFarmerId, response.entityId());
        assertEquals(lockedFarmerId, response.targetUserId());
        assertTrue(response.details().contains("Gian lận nguồn gốc sản phẩm nhiều lần"));
        assertEquals(recordedTime, response.createdAt());
    }

    @Test
    @DisplayName("TC-LOG-01 (1b): Ghi log thao tác quản trị hoàn tiền và gỡ sản phẩm đầy đủ thông tin truy vết")
    void logRefundAndProductRemoval_RecordsCompleteTraceabilityMetadata() {
        Long adminId = 1L;
        Long orderId = 801L;
        Long refundId = 301L;
        Long buyerId = 20L;
        Long productId = 1001L;
        Long farmerId = 10L;

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(inv -> {
            AuditLog log = inv.getArgument(0);
            log.setId(log.getAction().equals("APPROVE_REFUND") ? 5002L : 5003L);
            log.setCreatedAt(OffsetDateTime.now());
            return log;
        });

        // 1. Thao tác hoàn tiền của Admin
        AuditLogResponse refundAudit = auditLogService.logAdminRefundApproval(
            adminId,
            orderId,
            refundId,
            buyerId,
            new BigDecimal("125000.00"),
            "Sản phẩm hư hỏng quá 40% khi đồng kiểm"
        );

        assertEquals(adminId, refundAudit.actorId());
        assertEquals("APPROVE_REFUND", refundAudit.action());
        assertEquals("refunds", refundAudit.entityType());
        assertEquals(refundId, refundAudit.entityId());
        assertEquals(orderId, refundAudit.orderId());
        assertEquals(buyerId, refundAudit.targetUserId());
        assertNotNull(refundAudit.createdAt());

        // 2. Thao tác gỡ sản phẩm vi phạm của Admin
        AuditLogResponse productAudit = auditLogService.logAdminProductRemoval(
            adminId,
            productId,
            farmerId,
            "Sử dụng hình ảnh giả mạo chứng nhận VietGAP"
        );

        assertEquals(adminId, productAudit.actorId());
        assertEquals("REMOVE_PRODUCT", productAudit.action());
        assertEquals("products", productAudit.entityType());
        assertEquals(productId, productAudit.entityId());
        assertEquals(farmerId, productAudit.targetUserId());
        assertNotNull(productAudit.createdAt());
    }

    @Test
    @DisplayName("TC-LOG-01 (2a): Ghi nhận giao dịch tài chính và tra cứu lại đầy đủ theo đơn hàng (orderId)")
    void recordAndQueryFinancialLogsByOrderId_ReturnsOrderPaymentAndRefundHistory() {
        Long orderId = 801L;
        Long buyerId = 20L;
        Long adminId = 1L;

        when(financialLogRepository.save(any(FinancialTransactionLog.class))).thenAnswer(inv -> {
            FinancialTransactionLog log = inv.getArgument(0);
            log.setId(9001L);
            log.setCreatedAt(OffsetDateTime.now());
            return log;
        });
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

        // 1. Ghi log giao dịch tài chính (và kiểm tra tự động phản chiếu sang audit_logs)
        FinancialTransactionLogResponse recorded = auditLogService.recordFinancialTransaction(
            orderId,
            buyerId,
            adminId,
            "DISPUTE_REFUND",
            "WALLET_REFUND",
            new BigDecimal("125000.00"),
            "SUCCESS",
            "REF-2026-801",
            "Hoàn tiền phân xử tranh chấp #45"
        );

        assertEquals(9001L, recorded.id());
        assertEquals(orderId, recorded.orderId());
        assertEquals(buyerId, recorded.userId());
        assertEquals(new BigDecimal("125000.00"), recorded.amount());
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));

        // 2. Tra cứu lịch sử giao dịch tài chính theo đơn hàng #801
        FinancialTransactionLog depositLog = FinancialTransactionLog.builder()
            .id(9000L)
            .orderId(orderId)
            .userId(buyerId)
            .actorId(buyerId)
            .transactionType("DEPOSIT")
            .paymentMethod("ONLINE")
            .amount(new BigDecimal("51000.00"))
            .status("SUCCESS")
            .providerRef("PAY-2026-801")
            .createdAt(OffsetDateTime.now().minusDays(2))
            .build();

        FinancialTransactionLog refundLog = FinancialTransactionLog.builder()
            .id(9001L)
            .orderId(orderId)
            .userId(buyerId)
            .actorId(adminId)
            .transactionType("DISPUTE_REFUND")
            .paymentMethod("WALLET_REFUND")
            .amount(new BigDecimal("125000.00"))
            .status("SUCCESS")
            .providerRef("REF-2026-801")
            .createdAt(OffsetDateTime.now())
            .build();

        when(financialLogRepository.findAllByOrderIdOrderByCreatedAtDescIdDesc(orderId))
            .thenReturn(List.of(refundLog, depositLog));

        List<FinancialTransactionLogResponse> orderLogs = auditLogService.getFinancialLogsByOrderId(orderId);

        assertEquals(2, orderLogs.size());
        assertEquals("DISPUTE_REFUND", orderLogs.get(0).transactionType());
        assertEquals("DEPOSIT", orderLogs.get(1).transactionType());
        assertTrue(orderLogs.stream().allMatch(item -> item.orderId().equals(orderId)));
    }

    @Test
    @DisplayName("TC-LOG-01 (2b): Tra cứu log giao dịch tài chính theo người dùng (userId) và phân trang")
    void searchFinancialLogsByUserId_ReturnsUserTransactionHistory() {
        Long buyerId = 20L;

        FinancialTransactionLog tx1 = FinancialTransactionLog.builder()
            .id(9010L)
            .orderId(802L)
            .userId(buyerId)
            .actorId(buyerId)
            .transactionType("FULL_PAYMENT")
            .paymentMethod("ONLINE")
            .amount(new BigDecimal("340000.00"))
            .status("SUCCESS")
            .providerRef("VNPAY-998877")
            .createdAt(OffsetDateTime.now())
            .build();

        when(financialLogRepository.searchFinancialLogs(
            isNull(),
            eq(buyerId),
            isNull(),
            isNull(),
            isNull(),
            isNull(),
            eq(PageRequest.of(0, 20))
        )).thenReturn(new PageImpl<>(List.of(tx1), PageRequest.of(0, 20), 1));

        PagedLogResponse<FinancialTransactionLogResponse> result = auditLogService.searchFinancialLogs(
            null, buyerId, null, null, null, null, 0, 20
        );

        assertEquals(1L, result.totalElements());
        assertEquals(1, result.items().size());
        assertEquals(buyerId, result.items().get(0).userId());
        assertEquals(802L, result.items().get(0).orderId());
        assertEquals(new BigDecimal("340000.00"), result.items().get(0).amount());
    }

    @Test
    @DisplayName("TC-LOG-01 (2c): Chặn khoảng thời gian tra cứu không hợp lệ (from > to)")
    void searchLogs_InvalidTimeRange_ThrowsBadRequest() {
        OffsetDateTime now = OffsetDateTime.now();

        BusinessException ex = assertThrows(
            BusinessException.class,
            () -> auditLogService.searchAdminAuditLogs(
                1L, null, null, null, null, null, now, now.minusHours(2), 0, 20
            )
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("INVALID_TIME_RANGE", ex.getCode());
        verify(auditLogRepository, never()).searchLogs(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }
}
