package com.nsv.backend.user.service;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.user.dto.CreateFarmRequest;
import com.nsv.backend.user.dto.FarmResponse;
import com.nsv.backend.user.entity.Farm;
import com.nsv.backend.user.repository.FarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FarmServiceImpl implements FarmService {

    private final FarmRepository farmRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public FarmResponse createMinimalFarmProfile(Long farmerId, CreateFarmRequest request) {
        if (farmRepository.existsByUserId(farmerId)) {
            throw new BusinessException(
                HttpStatus.CONFLICT,
                "FARM_ALREADY_EXISTS",
                "Tài khoản nông dân này đã có hồ sơ trang trại"
            );
        }

        validateReferenceExists("provinces", request.provinceId(), "Tỉnh/thành phố không tồn tại");
        validateReferenceExists("categories", request.mainCategoryId(), "Loại nông sản không tồn tại");

        Farm farm = Farm.builder()
            .userId(farmerId)
            .farmName(request.farmName().trim())
            .provinceId(request.provinceId())
            .addressLine(request.addressLine().trim())
            .mainCategoryId(request.mainCategoryId())
            .certificateStatus("NONE")
            .build();

        return FarmResponse.from(farmRepository.save(farm));
    }

    private void validateReferenceExists(String tableName, Long id, String errorMessage) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM " + tableName + " WHERE id = ?",
            Integer.class,
            id
        );
        if (count == null || count == 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_REFERENCE", errorMessage);
        }
    }
}
