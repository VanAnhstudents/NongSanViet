package com.nsv.backend.product.controller;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.product.dto.ComboResponse;
import com.nsv.backend.product.dto.CreateComboRequest;
import com.nsv.backend.product.service.ComboService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/combos")
@RequiredArgsConstructor
public class ComboController {

    private final ComboService comboService;

    @PostMapping
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ComboResponse> createCombo(
            Authentication authentication,
            @Valid @RequestBody CreateComboRequest request
    ) {
        Long farmerId = extractUserId(authentication);
        ComboResponse response = comboService.createCombo(farmerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComboResponse> getComboById(@PathVariable Long id) {
        return ResponseEntity.ok(comboService.getComboById(id));
    }

    @GetMapping
    public ResponseEntity<List<ComboResponse>> getActiveCombos() {
        return ResponseEntity.ok(comboService.getActiveCombos());
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
