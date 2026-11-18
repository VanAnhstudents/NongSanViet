package com.nsv.backend.review.controller;

import com.nsv.backend.common.security.AuthenticatedUser;
import com.nsv.backend.review.dto.CreateReviewRequest;
import com.nsv.backend.review.dto.ProductReviewsResponse;
import com.nsv.backend.review.dto.ReviewResponse;
import com.nsv.backend.review.service.ReviewService;
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

/** PBI-36 (đường dẫn /api/v1/buyer/** chỉ dành cho BUYER). */
@RestController
@RequestMapping("/api/v1/buyer")
@RequiredArgsConstructor
public class BuyerReviewController {

    private final ReviewService reviewService;

    @PostMapping("/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse create(@AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateReviewRequest request) {
        return reviewService.create(user.id(), request);
    }

    @GetMapping("/products/{productId}/reviews")
    public ProductReviewsResponse productReviews(@PathVariable Long productId) {
        return reviewService.getProductReviews(productId);
    }
}
