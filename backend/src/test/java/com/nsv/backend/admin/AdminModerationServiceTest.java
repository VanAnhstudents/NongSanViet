package com.nsv.backend.admin;

import com.nsv.backend.admin.dto.AccountLockResponse;
import com.nsv.backend.admin.dto.ProductRemovalResponse;
import com.nsv.backend.admin.service.AdminModerationService;
import com.nsv.backend.admin.service.AuditLogService;
import com.nsv.backend.product.service.ProductModerationService;
import com.nsv.backend.user.service.UserAccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminModerationServiceTest {

    @Mock
    private UserAccountService userAccountService;

    @Mock
    private ProductModerationService productModerationService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AdminModerationService service;

    @Test
    @DisplayName("TC-ADMIN-01: Admin khóa tài khoản vi phạm -> LOCKED và ghi nhật ký USER_LOCKED")
    void tc01_lockUserIsAudited() {
        AccountLockResponse result = service.lockUser(1L, 5L);

        assertThat(result.userId()).isEqualTo(5L);
        assertThat(result.status()).isEqualTo("LOCKED");
        verify(userAccountService).lock(5L);
        verify(auditLogService).record(1L, "USER_LOCKED", "USER", 5L);
    }

    @Test
    @DisplayName("TC-ADMIN-01: Admin gỡ sản phẩm vi phạm -> các lô bị đóng và ghi nhật ký PRODUCT_REMOVED")
    void tc01_removeProductClosesBatches() {
        when(productModerationService.removeFromSale(3L)).thenReturn(2);

        ProductRemovalResponse result = service.removeProduct(1L, 3L);

        assertThat(result.closedBatches()).isEqualTo(2);
        verify(auditLogService).record(1L, "PRODUCT_REMOVED", "PRODUCT", 3L);
    }
}
