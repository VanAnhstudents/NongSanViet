package com.nsv.backend.dispute.controller;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.dispute.dto.*;
import com.nsv.backend.dispute.service.DisputeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DisputeController {

    private final DisputeService disputeService;

    // Buyer kiểm tra điều kiện và thời hạn 24h gửi khiếu nại sau khi nhận hàng
    @GetMapping("/orders/{orderId}/complaint-eligibility")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ComplaintEligibilityResponse> checkComplaintEligibility(
            Authentication authentication,
            @PathVariable Long orderId
    ) {
        Long buyerId = extractUserId(authentication);
        return ResponseEntity.ok(disputeService.checkComplaintEligibility(buyerId, orderId));
    }

    // Buyer gửi khiếu nại (JSON) kèm tối thiểu 1 ảnh/video trong vòng 24h sau khi nhận hàng
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

    // Buyer gửi khiếu nại kèm tải trực tiếp tệp ảnh/video (multipart/form-data) trong vòng 24h sau nhận hàng
    @PostMapping(value = "/orders/{orderId}/complaints/multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ComplaintSubmissionResponse> createComplaintWithMedia(
            Authentication authentication,
            @PathVariable Long orderId,
            @RequestParam("reason") String reason,
            @RequestParam("description") String description,
            @RequestPart("files") List<MultipartFile> files,
            @RequestParam(value = "captions", required = false) List<String> captions
    ) {
        Long buyerId = extractUserId(authentication);
        ComplaintSubmissionResponse response = disputeService.createComplaintWithMedia(
            buyerId, orderId, reason, description, files, captions
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Buyer xem chi tiết khiếu nại đã gửi cho đơn hàng
    @GetMapping("/orders/{orderId}/complaint")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ComplaintSubmissionResponse> getBuyerComplaintByOrder(
            Authentication authentication,
            @PathVariable Long orderId
    ) {
        Long buyerId = extractUserId(authentication);
        return ResponseEntity.ok(disputeService.getBuyerComplaintByOrder(buyerId, orderId));
    }

    // Buyer xem danh sách toàn bộ khiếu nại của mình
    @GetMapping("/buyer/complaints")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<List<ComplaintSubmissionResponse>> getBuyerComplaints(Authentication authentication) {
        Long buyerId = extractUserId(authentication);
        return ResponseEntity.ok(disputeService.getBuyerComplaints(buyerId));
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
