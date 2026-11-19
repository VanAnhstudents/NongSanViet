package com.nsv.backend.review.service;

import com.nsv.backend.common.exception.ApiException;
import com.nsv.backend.review.dto.FarmerReputationResponse;
import com.nsv.backend.review.repository.ReputationRepository;
import com.nsv.backend.review.repository.ReputationStats;
import com.nsv.backend.user.dto.UserDto;
import com.nsv.backend.user.entity.Role;
import com.nsv.backend.user.service.UserLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PBI-38: chỉ số uy tín người bán, tính tự động từ reviews và orders mỗi lần xem
 * (nên tự cập nhật khi có đánh giá mới hoặc đơn hàng hoàn tất mới).
 */
@Service
@RequiredArgsConstructor
public class ReputationService {

    private final ReputationRepository reputationRepository;
    private final UserLookupService userLookupService;

    @Transactional(readOnly = true)
    public FarmerReputationResponse getReputation(Long farmerId) {
        UserDto farmer = userLookupService.findActiveById(farmerId)
                .filter(u -> u.role() == Role.FARMER)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy người bán"));

        ReputationStats stats = reputationRepository.findStatsByFarmer(farmerId);
        double average = stats.reviewCount() == 0 ? 0.0 : (double) stats.ratingSum() / stats.reviewCount();
        double goodRate = stats.reviewCount() == 0 ? 0.0 : stats.goodReviewCount() * 100.0 / stats.reviewCount();
        return new FarmerReputationResponse(farmerId, farmer.fullName(), round1(average),
                (int) stats.reviewCount(), round1(goodRate), (int) stats.completedOrders());
    }

    private static double round1(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
