package com.nsv.backend.order.controller;

import com.nsv.backend.common.security.AuthenticatedUser;
import com.nsv.backend.order.dto.ShortageRefundResponse;
import com.nsv.backend.order.dto.ShortageRequest;
import com.nsv.backend.order.service.PreorderRefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** PBI-32 (đường dẫn /api/v1/farmer/** chỉ dành cho FARMER). */
@RestController
@RequestMapping("/api/v1/farmer/orders")
@RequiredArgsConstructor
public class FarmerShortageController {

    private final PreorderRefundService preorderRefundService;

    @PostMapping("/{orderId}/report-shortage")
    public ShortageRefundResponse reportShortage(@AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long orderId, @Valid @RequestBody ShortageRequest request) {
        return preorderRefundService.reportShortage(user.id(), orderId, request.reason());
    }
}
