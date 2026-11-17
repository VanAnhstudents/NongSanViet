package com.nsv.backend.user.controller;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.user.dto.CreateFarmRequest;
import com.nsv.backend.user.dto.FarmResponse;
import com.nsv.backend.user.service.FarmService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/farms")
@RequiredArgsConstructor
public class FarmController {

    private final FarmService farmService;

    @PostMapping
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<FarmResponse> createFarm(
            Authentication authentication,
            @Valid @RequestBody CreateFarmRequest request
    ) {
        Long farmerId = extractUserId(authentication);
        FarmResponse response = farmService.createMinimalFarmProfile(farmerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
}
