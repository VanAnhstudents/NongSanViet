package com.nsv.backend.order.controller;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.order.dto.AddToCartRequest;
import com.nsv.backend.order.dto.CartResponse;
import com.nsv.backend.order.dto.UpdateCartItemRequest;
import com.nsv.backend.order.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@PreAuthorize("hasRole('BUYER')")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(Authentication authentication) {
        return ResponseEntity.ok(cartService.getCart(extractUserId(authentication)));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            Authentication authentication,
            @Valid @RequestBody AddToCartRequest request
    ) {
        CartResponse response = cartService.addItem(extractUserId(authentication), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            Authentication authentication,
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        return ResponseEntity.ok(
            cartService.updateItemQuantity(extractUserId(authentication), cartItemId, request)
        );
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> removeItem(
            Authentication authentication,
            @PathVariable Long cartItemId
    ) {
        return ResponseEntity.ok(cartService.removeItem(extractUserId(authentication), cartItemId));
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
