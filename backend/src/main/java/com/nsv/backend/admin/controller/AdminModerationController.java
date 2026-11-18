package com.nsv.backend.admin.controller;

import com.nsv.backend.admin.dto.AccountLockResponse;
import com.nsv.backend.admin.dto.ProductRemovalResponse;
import com.nsv.backend.admin.service.AdminModerationService;
import com.nsv.backend.common.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** PBI-33 (đường dẫn /api/v1/admin/** chỉ dành cho ADMIN). */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminModerationController {

    private final AdminModerationService adminModerationService;

    @PutMapping("/users/{id}/lock")
    public AccountLockResponse lockUser(@AuthenticationPrincipal AuthenticatedUser admin, @PathVariable Long id) {
        return adminModerationService.lockUser(admin.id(), id);
    }

    @PutMapping("/products/{id}/remove")
    public ProductRemovalResponse removeProduct(@AuthenticationPrincipal AuthenticatedUser admin,
            @PathVariable Long id) {
        return adminModerationService.removeProduct(admin.id(), id);
    }
}
