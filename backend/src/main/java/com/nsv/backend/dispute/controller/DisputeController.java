package com.nsv.backend.dispute.controller;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.dispute.dto.*;
import com.nsv.backend.dispute.service.DisputeService;
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
public class DisputeController {

    private final DisputeService disputeService;

    // Buyer tạo khiếu nại kèm bằng chứng ảnh/video
    @PostMapping("/orders/{orderId}/complaints")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<DisputeDetailResponse> createComplaint(
            Authentication authentication,
            @PathVariable Long orderId,
            @Valid @RequestBody CreateComplaintRequest request
    ) {
        Long buyerId = extractUserId(authentication);
        DisputeDetailResponse response = disputeService.createComplaint(buyerId, orderId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Farmer gửi phản hồi giải trình và bằng chứng đối chứng
    @PostMapping("/farmer/disputes/{id}/response")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<DisputeDetailResponse> submitFarmerResponse(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody FarmerDisputeResponseRequest request
    ) {
        Long farmerId = extractUserId(authentication);
        return ResponseEntity.ok(disputeService.submitFarmerResponse(farmerId, id, request));
    }

    // Admin xem danh sách tất cả hồ sơ tranh chấp
    @GetMapping("/admin/disputes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DisputeDetailResponse>> getAllDisputes(
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(disputeService.getAllDisputesForAdmin(status));
    }

    // Admin xem đầy đủ bằng chứng (ảnh/video, nhật ký trồng trọt, nhật ký hệ thống, đồng kiểm, thanh toán)
    @GetMapping("/admin/disputes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DisputeDetailResponse> getDisputeDetail(@PathVariable Long id) {
        return ResponseEntity.ok(disputeService.getDisputeDetailForAdmin(id));
    }

    // Admin ra quyết định phân xử và tự động thực hiện hoàn tiền nếu chấp nhận khiếu nại
    @PostMapping("/admin/disputes/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DisputeDetailResponse> resolveDispute(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ResolveDisputeRequest request
    ) {
        Long adminId = extractUserId(authentication);
        return ResponseEntity.ok(disputeService.resolveDisputeByAdmin(adminId, id, request));
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
