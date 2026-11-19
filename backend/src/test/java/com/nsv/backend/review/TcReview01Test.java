package com.nsv.backend.review;

import com.nsv.backend.common.exception.ApiException;
import com.nsv.backend.review.dto.CreateReviewRequest;
import com.nsv.backend.review.dto.ProductReviewsResponse;
import com.nsv.backend.review.dto.ReviewResponse;
import com.nsv.backend.review.entity.Review;
import com.nsv.backend.review.repository.ProductReviewRow;
import com.nsv.backend.review.repository.ReviewQueryRepository;
import com.nsv.backend.review.repository.ReviewRepository;
import com.nsv.backend.review.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TcReview01Test {

    private static final long BUYER_ID = 5L;
    private static final long ORDER_ITEM_ID = 11L;
    private static final Instant NOW = Instant.parse("2026-11-17T03:00:00Z");

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewQueryRepository reviewQueryRepository;

    private ReviewService service;

    @BeforeEach
    void setUp() {
        service = new ReviewService(reviewRepository, reviewQueryRepository,
                Clock.fixed(NOW, ZoneId.of("Asia/Ho_Chi_Minh")));
        when(reviewRepository.existsByOrderItemId(ORDER_ITEM_ID)).thenReturn(false);
        when(reviewRepository.saveAndFlush(any(Review.class))).thenAnswer(inv -> {
            Review r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });
    }

    private void givenOrderStatus(String status) {
        when(reviewQueryRepository.findOrderStatusForBuyer(ORDER_ITEM_ID, BUYER_ID))
                .thenReturn(Optional.of(status));
    }

    @Test
    @DisplayName("TC-REVIEW-01: Đơn đã giao (DELIVERED) -> Buyer đánh giá 5 sao kèm bình luận thành công")
    void tc01_reviewAfterDelivered() {
        givenOrderStatus("DELIVERED");

        ReviewResponse result = service.create(BUYER_ID,
                new CreateReviewRequest(ORDER_ITEM_ID, 5, "  Cà chua tươi, giao nhanh  "));

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.rating()).isEqualTo(5);
        assertThat(result.comment()).isEqualTo("Cà chua tươi, giao nhanh");
        assertThat(result.createdAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("TC-REVIEW-01: Đơn chưa giao (SHIPPING) -> không đánh giá được")
    void tc01_cannotReviewBeforeDelivered() {
        givenOrderStatus("SHIPPING");

        assertThatThrownBy(() -> service.create(BUYER_ID, new CreateReviewRequest(ORDER_ITEM_ID, 4, "Ngon")))
                .isInstanceOf(ApiException.class);

        verify(reviewRepository, never()).saveAndFlush(any(Review.class));
    }

    @Test
    @DisplayName("TC-REVIEW-01: Trang sản phẩm hiển thị số sao trung bình và các bình luận")
    void tc01_productPageShowsAverageAndComments() {
        when(reviewQueryRepository.productExists(3L)).thenReturn(true);
        when(reviewQueryRepository.findByProduct(3L)).thenReturn(List.of(
                new ProductReviewRow(3L, 5, "Rất tươi", NOW, "Cô Lan"),
                new ProductReviewRow(2L, 4, "Ổn", NOW, "Anh Minh"),
                new ProductReviewRow(1L, 3, null, NOW, "Chị Hoa")));

        ProductReviewsResponse result = service.getProductReviews(3L);

        assertThat(result.reviewCount()).isEqualTo(3);
        assertThat(result.averageRating()).isEqualTo(4.0);
        assertThat(result.reviews()).extracting(r -> r.comment()).containsExactly("Rất tươi", "Ổn", null);
        assertThat(result.reviews().get(0).reviewerName()).isEqualTo("Cô Lan");
    }

    @Test
    @DisplayName("TC-REVIEW-01: Sản phẩm chưa có đánh giá -> trung bình 0, không có bình luận")
    void tc01_productWithoutReviews() {
        when(reviewQueryRepository.productExists(3L)).thenReturn(true);
        when(reviewQueryRepository.findByProduct(3L)).thenReturn(List.of());

        ProductReviewsResponse result = service.getProductReviews(3L);

        assertThat(result.reviewCount()).isZero();
        assertThat(result.averageRating()).isEqualTo(0.0);
        assertThat(result.reviews()).isEmpty();
    }
}
