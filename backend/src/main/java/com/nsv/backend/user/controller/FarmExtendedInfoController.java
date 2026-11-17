package com.nsv.backend.user.controller;

import com.nsv.backend.common.security.AuthenticatedUser;
import com.nsv.backend.user.dto.FarmExtendedInfoRequest;
import com.nsv.backend.user.dto.FarmExtendedInfoResponse;
import com.nsv.backend.user.service.FarmExtendedInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** PBI-11: thông tin mở rộng của hồ sơ trang trại (chỉ Farmer, theo đường dẫn /api/v1/farmer/**). */
@RestController
@RequestMapping("/api/v1/farmer/farm/extended-info")
@RequiredArgsConstructor
public class FarmExtendedInfoController {

    private final FarmExtendedInfoService service;

    @GetMapping
    public FarmExtendedInfoResponse get(@AuthenticationPrincipal AuthenticatedUser user) {
        return service.get(user.id());
    }

    @PutMapping
    public FarmExtendedInfoResponse update(@AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody(required = false) FarmExtendedInfoRequest request) {
        return service.update(user.id(), request);
    }
}
