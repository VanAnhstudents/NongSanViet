package com.nsv.backend.order.controller;

import com.nsv.backend.common.security.AuthenticatedUser;
import com.nsv.backend.order.dto.PaymentCreateRequest;
import com.nsv.backend.order.dto.PaymentResponse;
import com.nsv.backend.order.dto.PaymentSimulateRequest;
import com.nsv.backend.order.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** PBI-18: thanh toán của Buyer (đường dẫn /api/v1/buyer/** chỉ dành cho vai trò BUYER). */
@RestController
@RequestMapping("/api/v1/buyer")
@RequiredArgsConstructor
public class BuyerPaymentController {

    private final PaymentService paymentService;

    @PostMapping("/orders/{orderId}/payments")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse choosePaymentMethod(@AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long orderId, @Valid @RequestBody PaymentCreateRequest request) {
        return paymentService.choosePaymentMethod(user.id(), orderId, request.paymentMethod());
    }

    @PostMapping("/payments/{paymentId}/simulate")
    public PaymentResponse simulate(@AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long paymentId, @Valid @RequestBody PaymentSimulateRequest request) {
        return paymentService.simulateOnlinePayment(user.id(), paymentId, request.outcome());
    }

    @GetMapping("/orders/{orderId}/payment")
    public PaymentResponse latest(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long orderId) {
        return paymentService.getLatest(user.id(), orderId);
    }
}
