package com.nsv.backend.common.audit.service;

import com.nsv.backend.common.audit.dto.AuditLogResponse;
import com.nsv.backend.common.audit.dto.FinancialTransactionLogResponse;
import com.nsv.backend.common.audit.dto.PagedLogResponse;
import com.nsv.backend.common.audit.entity.AuditLog;
import com.nsv.backend.common.audit.entity.FinancialTransactionLog;
import com.nsv.backend.common.audit.repository.AuditLogRepository;
import com.nsv.backend.common.audit.repository.FinancialTransactionLogRepository;
import com.nsv.backend.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final FinancialTransactionLogRepository financialLogRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public AuditLogResponse recordAdminAction(
            Long adminId,
            String action,
            String entityType,
            Long entityId,
            Long targetUserId,
            Long orderId,
            String details
    ) {
        if (adminId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_ACTOR", "Thiếu định danh người thực hiện thao tác");
        }
        if (!StringUtils.hasText(action) || !StringUtils.hasText(entityType)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_AUDIT_PAYLOAD", "Thiếu loại hành động hoặc đối tượng tác động");
        }

        AuditLog logEntry = AuditLog.builder()
            .actorId(adminId)
            .actorRole("ADMIN")
            .category("ADMIN_ACTION")
            .action(action.trim().toUpperCase())
            .entityType(entityType.trim().toLowerCase())
            .entityId(entityId)
            .targetUserId(targetUserId)
            .orderId(orderId)
            .details(StringUtils.hasText(details) ? details.trim() : null)
            .build();

        return AuditLogResponse.from(auditLogRepository.save(logEntry));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public AuditLogResponse logUserAccountLockChange(
            Long adminId,
            Long targetUserId,
            boolean locked,
            String reason
    ) {
        String action = locked ? "LOCK_USER_ACCOUNT" : "UNLOCK_USER_ACCOUNT";
        String summary = (locked ? "Khóa tài khoản người dùng #" : "Mở khóa tài khoản người dùng #")
            + targetUserId
            + (StringUtils.hasText(reason) ? ". Lý do: " + reason.trim() : "");

        return recordAdminAction(adminId, action, "users", targetUserId, targetUserId, null, summary);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public AuditLogResponse logAdminRefundApproval(
            Long adminId,
            Long orderId,
            Long refundId,
            Long recipientUserId,
            BigDecimal refundAmount,
            String reason
    ) {
        BigDecimal normalizedAmount = refundAmount != null
            ? refundAmount.setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        String summary = String.format(
            "Admin phê duyệt hoàn tiền %s VND cho đơn hàng #%d (Người nhận #%d). Lý do: %s",
            normalizedAmount,
            orderId,
            recipientUserId,
            StringUtils.hasText(reason) ? reason.trim() : "Theo quyết định phân xử"
        );

        return recordAdminAction(adminId, "APPROVE_REFUND", "refunds", refundId, recipientUserId, orderId, summary);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public AuditLogResponse logAdminProductRemoval(
            Long adminId,
            Long productId,
            Long farmerId,
            String reason
    ) {
        String summary = String.format(
            "Admin gỡ sản phẩm #%d của Farmer #%d khỏi sàn. Lý do: %s",
            productId,
            farmerId,
            StringUtils.hasText(reason) ? reason.trim() : "Vi phạm quy định đăng bán"
        );

        return recordAdminAction(adminId, "REMOVE_PRODUCT", "products", productId, farmerId, null, summary);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public FinancialTransactionLogResponse recordFinancialTransaction(
            Long orderId,
            Long userId,
            Long actorId,
            String transactionType,
            String paymentMethod,
            BigDecimal amount,
            String status,
            String providerRef,
            String note
    ) {
        if (orderId == null || userId == null || actorId == null) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "INVALID_FINANCIAL_LOG_KEYS",
                "Log giao dịch tài chính bắt buộc có orderId, userId và actorId"
            );
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "INVALID_FINANCIAL_AMOUNT",
                "Số tiền giao dịch tài chính phải lớn hơn 0"
            );
        }

        BigDecimal normalizedAmount = amount.setScale(2, RoundingMode.HALF_UP);
        String normalizedType = transactionType.trim().toUpperCase();
        String normalizedStatus = status.trim().toUpperCase();

        FinancialTransactionLog finLog = FinancialTransactionLog.builder()
            .orderId(orderId)
            .userId(userId)
            .actorId(actorId)
            .transactionType(normalizedType)
            .paymentMethod(paymentMethod.trim().toUpperCase())
            .amount(normalizedAmount)
            .status(normalizedStatus)
            .providerRef(StringUtils.hasText(providerRef) ? providerRef.trim() : null)
            .note(StringUtils.hasText(note) ? note.trim() : null)
            .build();

        FinancialTransactionLog savedFinLog = financialLogRepository.save(finLog);

        // Đồng thời ghi nhận vào bảng audit_logs để liên thông truy vết toàn hệ thống
        AuditLog mirroredAudit = AuditLog.builder()
            .actorId(actorId)
            .actorRole(actorId.equals(userId) ? "BUYER" : "ADMIN")
            .category("FINANCIAL_TX")
            .action("FIN_" + normalizedType + "_" + normalizedStatus)
            .entityType("financial_transaction_logs")
            .entityId(savedFinLog.getId())
            .orderId(orderId)
            .targetUserId(userId)
            .details(String.format(
                "Giao dịch %s (%s) số tiền %s VND cho đơn hàng #%d - Trạng thái: %s",
                normalizedType, finLog.getPaymentMethod(), normalizedAmount, orderId, normalizedStatus
            ))
            .build();
        auditLogRepository.save(mirroredAudit);

        return FinancialTransactionLogResponse.from(savedFinLog);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedLogResponse<AuditLogResponse> searchAdminAuditLogs(
            Long actorId,
            String category,
            String action,
            String entityType,
            Long targetUserId,
            Long orderId,
            OffsetDateTime fromTime,
            OffsetDateTime toTime,
            int page,
            int size
    ) {
        validateQueryParams(fromTime, toTime, page, size);

        Page<AuditLog> result = auditLogRepository.searchLogs(
            actorId,
            normalizeUpper(category),
            normalizeUpper(action),
            normalizeLower(entityType),
            targetUserId,
            orderId,
            fromTime,
            toTime,
            PageRequest.of(page, size)
        );

        return PagedLogResponse.fromPage(result, AuditLogResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedLogResponse<FinancialTransactionLogResponse> searchFinancialLogs(
            Long orderId,
            Long userId,
            String transactionType,
            String status,
            OffsetDateTime fromTime,
            OffsetDateTime toTime,
            int page,
            int size
    ) {
        validateQueryParams(fromTime, toTime, page, size);

        Page<FinancialTransactionLog> result = financialLogRepository.searchFinancialLogs(
            orderId,
            userId,
            normalizeUpper(transactionType),
            normalizeUpper(status),
            fromTime,
            toTime,
            PageRequest.of(page, size)
        );

        return PagedLogResponse.fromPage(result, FinancialTransactionLogResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinancialTransactionLogResponse> getFinancialLogsByOrderId(Long orderId) {
        return financialLogRepository.findAllByOrderIdOrderByCreatedAtDescIdDesc(orderId).stream()
            .map(FinancialTransactionLogResponse::from)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinancialTransactionLogResponse> getFinancialLogsByUserId(Long userId) {
        return financialLogRepository.findAllByUserIdOrderByCreatedAtDescIdDesc(userId).stream()
            .map(FinancialTransactionLogResponse::from)
            .toList();
    }

    private void validateQueryParams(OffsetDateTime fromTime, OffsetDateTime toTime, int page, int size) {
        if (fromTime != null && toTime != null && fromTime.isAfter(toTime)) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "INVALID_TIME_RANGE",
                "Thời điểm bắt đầu (from) không được lớn hơn thời điểm kết thúc (to)"
            );
        }
        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "INVALID_PAGINATION",
                "Tham số phân trang không hợp lệ (page >= 0, 1 <= size <= 100)"
            );
        }
    }

    private String normalizeUpper(String input) {
        return StringUtils.hasText(input) ? input.trim().toUpperCase() : null;
    }

    private String normalizeLower(String input) {
        return StringUtils.hasText(input) ? input.trim().toLowerCase() : null;
    }
}
