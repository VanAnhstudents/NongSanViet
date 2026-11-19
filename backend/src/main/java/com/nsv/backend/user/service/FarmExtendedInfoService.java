package com.nsv.backend.user.service;

import com.nsv.backend.common.exception.ApiException;
import com.nsv.backend.user.dto.FarmExtendedInfoRequest;
import com.nsv.backend.user.dto.FarmExtendedInfoResponse;
import com.nsv.backend.user.entity.Farm;
import com.nsv.backend.user.repository.FarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FarmExtendedInfoService {

    private final FarmRepository farmRepository;

    @Transactional(readOnly = true)
    public FarmExtendedInfoResponse get(long userId) {
        return toResponse(findFarm(userId));
    }

    /**
     * Lưu thông tin mở rộng. Trường bỏ trống (null hoặc chuỗi rỗng) vẫn được chấp nhận:
     * lúc đó chứng nhận được xóa và trạng thái về NONE.
     */
    @Transactional
    public FarmExtendedInfoResponse update(long userId, FarmExtendedInfoRequest request) {
        Farm farm = findFarm(userId);
        String newUrl = request == null ? null : normalize(request.certificateUrl());

        if (newUrl == null) {
            farm.setCertificateStatus("NONE");
        } else if (!Objects.equals(newUrl, farm.getCertificateUrl())
                || "NONE".equals(farm.getCertificateStatus())) {
            farm.setCertificateStatus("PENDING");
        }
        farm.setCertificateUrl(newUrl);

        return toResponse(farmRepository.save(farm));
    }

    private Farm findFarm(long userId) {
        return farmRepository.findByUserId(userId)
                .orElseThrow(() -> ApiException.notFound("Bạn chưa có hồ sơ trang trại. Vui lòng tạo hồ sơ trước."));
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static FarmExtendedInfoResponse toResponse(Farm farm) {
        return new FarmExtendedInfoResponse(farm.getId(), farm.getFarmName(),
                farm.getCertificateUrl(), farm.getCertificateStatus());
    }
}
