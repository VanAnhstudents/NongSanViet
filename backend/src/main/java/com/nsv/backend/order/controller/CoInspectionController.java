package com.nsv.backend.order.controller;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.order.dto.CoInspectionResponse;
import com.nsv.backend.order.dto.CreateCoInspectionRequest;
import com.nsv.backend.order.service.CoInspectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CoInspectionController {

    private final CoInspectionService coInspectionService;

    // Buyer gửi yêu cầu đồng kiểm trước khi xác nhận đã nhận hàng
    @PostMapping("/orders/{orderId}/co-inspection")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<CoInspectionResponse> requestCoInspection(
            Authentication authentication,
            @PathVariable Long orderId,
            @Valid @RequestBody CreateCoInspectionRequest request
    ) {
        Long buyerId = extractUserId(authentication);
        CoInspectionResponse response = coInspectionService.requestCoInspection(buyerId, orderId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Xem chi tiết yêu cầu đồng kiểm gắn với đơn hàng (Buyer, Farmer, Admin/điều phối giao hàng)
    @GetMapping("/orders/{orderId}/co-inspection")
    @PreAuthorize("hasAnyRole('BUYER', 'FARMER', 'ADMIN')")
    public ResponseEntity<CoInspectionResponse> getOrderCoInspection(
            Authentication authentication,
            @PathVariable Long orderId
    ) {
        Long actorId = extractUserId(authentication);
        String role = extractRole(authentication);
        return ResponseEntity.ok(coInspectionService.getOrderCoInspection(actorId, role, orderId));
    }

    // Farmer xem danh sách các yêu cầu đồng kiểm của trang trại mình để phối hợp giao hàng
    @GetMapping("/farmer/co-inspections")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<List<CoInspectionResponse>> getFarmerCoInspections(Authentication authentication) {
        Long farmerId = extractUserId(authentication);
        return ResponseEntity.ok(coInspectionService.getFarmerCoInspections(farmerId));
    }

    // Farmer xác nhận đã tiếp nhận và bàn giao chỉ thị đồng kiểm cho đơn vị giao hàng
    @PatchMapping("/farmer/orders/{orderId}/co-inspection/acknowledge")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<CoInspectionResponse> acknowledgeCoInspection(
            Authentication authentication,
            @PathVariable Long orderId
    ) {
        Long farmerId = extractUserId(authentication);
        return ResponseEntity.ok(coInspectionService.acknowledgeCoInspection(farmerId, orderId));
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

    private String extractRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(auth -> auth.startsWith("ROLE_"))
            .map(auth -> auth.substring(5))
            .findFirst()
            .orElse("");
    }
}
