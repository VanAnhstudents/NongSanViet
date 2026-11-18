package com.nsv.backend.admin.service;

import com.nsv.backend.admin.dto.AccountLockResponse;
import com.nsv.backend.admin.dto.ProductRemovalResponse;
import com.nsv.backend.common.exception.ApiException;
import com.nsv.backend.common.exception.FieldErrorDetail;
import com.nsv.backend.product.service.ProductModerationService;
import com.nsv.backend.user.service.UserAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** PBI-33: Admin khóa tài khoản vi phạm và gỡ sản phẩm không hợp lệ; mỗi thao tác được ghi vào audit_logs. */
@Service
@RequiredArgsConstructor
public class AdminModerationService {

    private final UserAccountService userAccountService;
    private final ProductModerationService productModerationService;
    private final AuditLogService auditLogService;

    @Transactional
    public AccountLockResponse lockUser(Long adminId, Long userId) {
        if (adminId.equals(userId)) {
            throw ApiException.badRequest("Dữ liệu không hợp lệ",
                    List.of(new FieldErrorDetail("id", "Không thể tự khóa tài khoản của mình")));
        }
        userAccountService.lock(userId);
        auditLogService.record(adminId, "USER_LOCKED", "USER", userId);
        return new AccountLockResponse(userId, "LOCKED");
    }

    @Transactional
    public ProductRemovalResponse removeProduct(Long adminId, Long productId) {
        int closed = productModerationService.removeFromSale(productId);
        auditLogService.record(adminId, "PRODUCT_REMOVED", "PRODUCT", productId);
        return new ProductRemovalResponse(productId, closed);
    }
}
