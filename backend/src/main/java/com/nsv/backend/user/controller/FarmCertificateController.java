package com.nsv.backend.user.controller;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.user.dto.*;
import com.nsv.backend.user.service.FarmCertificateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FarmCertificateController {

    private final FarmCertificateService certificateService;

    // Farmer nộp chứng nhận (VietGAP, Hữu cơ, OCOP...) kèm file/ảnh
    @PostMapping("/farmer/certificates")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<FarmCertificateResponse> submitCertificate(
            Authentication authentication,
            @Valid @RequestBody SubmitCertificateRequest request
    ) {
        Long farmerId = extractUserId(authentication);
        FarmCertificateResponse response = certificateService.submitCertificate(farmerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Farmer xem danh sách chứng nhận đã nộp và trạng thái xét duyệt
    @GetMapping("/farmer/certificates")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<List<FarmCertificateResponse>> getFarmerCertificates(Authentication authentication) {
        Long farmerId = extractUserId(authentication);
        return ResponseEntity.ok(certificateService.getFarmerCertificates(farmerId));
    }

    // Admin xem danh sách chứng nhận Farmer nộp (lọc theo PENDING / APPROVED / REJECTED)
    @GetMapping("/admin/certificates")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FarmCertificateResponse>> getCertificatesForAdmin(
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(certificateService.getCertificatesForAdmin(status));
    }

    // Admin phê duyệt hoặc từ chối từng chứng nhận
    @PatchMapping("/admin/certificates/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FarmCertificateResponse> reviewCertificate(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ReviewCertificateRequest request
    ) {
        Long adminId = extractUserId(authentication);
        return ResponseEntity.ok(certificateService.reviewCertificate(adminId, id, request));
    }

    // Public / Buyer xem hồ sơ trang trại kèm huy hiệu (badge) xác minh
    @GetMapping("/farms/{farmId}/profile")
    public ResponseEntity<FarmProfileResponse> getFarmProfile(@PathVariable Long farmId) {
        return ResponseEntity.ok(certificateService.getFarmProfileWithBadges(farmId));
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
