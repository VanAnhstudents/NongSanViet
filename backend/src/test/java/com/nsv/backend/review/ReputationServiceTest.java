package com.nsv.backend.review;

import com.nsv.backend.common.exception.ApiException;
import com.nsv.backend.review.dto.FarmerReputationResponse;
import com.nsv.backend.review.repository.ReputationRepository;
import com.nsv.backend.review.repository.ReputationStats;
import com.nsv.backend.review.service.ReputationService;
import com.nsv.backend.user.dto.UserDto;
import com.nsv.backend.user.entity.Role;
import com.nsv.backend.user.service.UserLookupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReputationServiceTest {

    private static final long FARMER_ID = 7L;

    @Mock
    private ReputationRepository reputationRepository;

    @Mock
    private UserLookupService userLookupService;

    private ReputationService service;

    @BeforeEach
    void setUp() {
        service = new ReputationService(reputationRepository, userLookupService);
    }

    private void givenFarmer() {
        when(userLookupService.findActiveById(FARMER_ID))
                .thenReturn(Optional.of(new UserDto(FARMER_ID, "Bác Ba", "bacba@example.com", "0912345678", Role.FARMER)));
    }

    @Test
    @DisplayName("TC-REP-01: Chỉ số uy tín tính tự động từ đánh giá và đơn hàng (sao TB, % đánh giá tốt, số đơn hoàn tất)")
    void tc01_computedFromReviewsAndOrders() {
        givenFarmer();
        when(reputationRepository.findStatsByFarmer(FARMER_ID)).thenReturn(new ReputationStats(4, 18, 3, 10));

        FarmerReputationResponse result = service.getReputation(FARMER_ID);

        assertThat(result.farmerName()).isEqualTo("Bác Ba");
        assertThat(result.averageRating()).isEqualTo(4.5);
        assertThat(result.reviewCount()).isEqualTo(4);
        assertThat(result.goodReviewRate()).isEqualTo(75.0);
        assertThat(result.completedOrders()).isEqualTo(10);
    }

    @Test
    @DisplayName("TC-REP-01: Farmer chưa có đánh giá -> sao trung bình 0, không lỗi chia cho 0")
    void tc01_noReviewsYet() {
        givenFarmer();
        when(reputationRepository.findStatsByFarmer(FARMER_ID)).thenReturn(new ReputationStats(0, 0, 0, 0));

        FarmerReputationResponse result = service.getReputation(FARMER_ID);

        assertThat(result.averageRating()).isEqualTo(0.0);
        assertThat(result.goodReviewRate()).isEqualTo(0.0);
        assertThat(result.reviewCount()).isZero();
    }

    @Test
    @DisplayName("TC-REP-01: Có đánh giá mới và đơn hoàn tất mới -> lần xem sau cập nhật ngay")
    void tc01_updatesWhenNewReviewOrCompletedOrder() {
        givenFarmer();
        when(reputationRepository.findStatsByFarmer(FARMER_ID))
                .thenReturn(new ReputationStats(2, 9, 2, 3))
                .thenReturn(new ReputationStats(3, 12, 2, 4));

        FarmerReputationResponse before = service.getReputation(FARMER_ID);
        FarmerReputationResponse after = service.getReputation(FARMER_ID);

        assertThat(before.averageRating()).isEqualTo(4.5);
        assertThat(before.completedOrders()).isEqualTo(3);
        assertThat(after.averageRating()).isEqualTo(4.0);
        assertThat(after.reviewCount()).isEqualTo(3);
        assertThat(after.completedOrders()).isEqualTo(4);
    }

    @Test
    @DisplayName("TC-REP-01: Tài khoản không phải Farmer -> không có chỉ số uy tín")
    void tc01_notAFarmer() {
        when(userLookupService.findActiveById(5L))
                .thenReturn(Optional.of(new UserDto(5L, "Cô Lan", "colan@example.com", "0988888888", Role.BUYER)));

        assertThatThrownBy(() -> service.getReputation(5L)).isInstanceOf(ApiException.class);
    }
}
