package com.nsv.backend.review.service;

import com.nsv.backend.common.exception.ApiException;
import com.nsv.backend.review.dto.CreateReviewRequest;
import com.nsv.backend.review.dto.ProductReviewsResponse;
import com.nsv.backend.review.dto.ReviewItem;
import com.nsv.backend.review.dto.ReviewResponse;
import com.nsv.backend.review.entity.Review;
import com.nsv.backend.review.repository.ProductReviewRow;
import com.nsv.backend.review.repository.ReviewQueryRepository;
import com.nsv.backend.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Set;

/** PBI-36: Buyer đánh giá và bình luận sản phẩm sau khi đơn hàng đã giao. */
@Service
@RequiredArgsConstructor
public class ReviewService {

    static final int MAX_LISTED = 50;
    private static final Set<String> REVIEWABLE_ORDER_STATUSES = Set.of("DELIVERED", "COMPLETED");

    private final ReviewRepository reviewRepository;
    private final ReviewQueryRepository reviewQueryRepository;
    private final Clock clock;

    @Transactional
    public ReviewResponse create(Long buyerId, CreateReviewRequest request) {
        String orderStatus = reviewQueryRepository.findOrderStatusForBuyer(request.orderItemId(), buyerId)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy sản phẩm trong đơn hàng của bạn"));
        if (!REVIEWABLE_ORDER_STATUSES.contains(orderStatus)) {
            throw invalidState("Chỉ đánh giá được sau khi đơn hàng đã giao");
        }
        if (reviewRepository.existsByOrderItemId(request.orderItemId())) {
            throw invalidState("Sản phẩm trong đơn hàng này đã được đánh giá");
        }

        String comment = request.comment() == null ? null : request.comment().trim();
        Review review = Review.builder()
                .orderItemId(request.orderItemId())
                .rating(request.rating().shortValue())
                .comment(comment == null || comment.isEmpty() ? null : comment)
                .createdAt(Instant.now(clock))
                .build();
        try {
            review = reviewRepository.saveAndFlush(review);
        } catch (DataIntegrityViolationException ex) {
            throw invalidState("Sản phẩm trong đơn hàng này đã được đánh giá");
        }
        return new ReviewResponse(review.getId(), review.getOrderItemId(), review.getRating(),
                review.getComment(), review.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public ProductReviewsResponse getProductReviews(Long productId) {
        if (!reviewQueryRepository.productExists(productId)) {
            throw ApiException.notFound("Không tìm thấy sản phẩm");
        }
        List<ProductReviewRow> rows = reviewQueryRepository.findByProduct(productId);
        double average = rows.stream().mapToInt(ProductReviewRow::rating).average().orElse(0.0);
        List<ReviewItem> items = rows.stream().limit(MAX_LISTED)
                .map(r -> new ReviewItem(r.id(), r.rating(), r.comment(), r.reviewerName(), r.createdAt()))
                .toList();
        return new ProductReviewsResponse(productId, Math.round(average * 10) / 10.0, rows.size(), items);
    }

    private ApiException invalidState(String message) {
        return new ApiException(HttpStatus.CONFLICT, "INVALID_STATE", message, null);
    }
}
