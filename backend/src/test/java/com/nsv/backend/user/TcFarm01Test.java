package com.nsv.backend.user;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.user.dto.CreateFarmRequest;
import com.nsv.backend.user.dto.FarmResponse;
import com.nsv.backend.user.entity.Farm;
import com.nsv.backend.user.repository.FarmRepository;
import com.nsv.backend.user.service.FarmServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TcFarm01Test {

    @Mock
    private FarmRepository farmRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private FarmServiceImpl farmService;

    @Test
    @DisplayName("TC-FARM-01: Tạo hồ sơ trang trại tối thiểu thành công khi đủ trường bắt buộc")
    void createMinimalFarmProfile_ValidInput_Success() {
        Long farmerId = 10L;
        CreateFarmRequest request = new CreateFarmRequest(
            "Trang trại Rau sạch Kim Thành", 30L, "Thôn Quỳnh Khê, Kim Thành", 1L
        );

        when(farmRepository.existsByUserId(farmerId)).thenReturn(false);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any())).thenReturn(1);
        when(farmRepository.save(any(Farm.class))).thenAnswer(inv -> {
            Farm farm = inv.getArgument(0);
            farm.setId(1L);
            return farm;
        });

        FarmResponse response = farmService.createMinimalFarmProfile(farmerId, request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(farmerId, response.userId());
        assertEquals("Trang trại Rau sạch Kim Thành", response.farmName());
        assertEquals("NONE", response.certificateStatus());
        verify(farmRepository, times(1)).save(any(Farm.class));
    }

    @Test
    @DisplayName("TC-FARM-01: Từ chối tạo nếu tài khoản nông dân đã có hồ sơ trang trại")
    void createMinimalFarmProfile_DuplicateFarm_ThrowsConflict() {
        Long farmerId = 10L;
        CreateFarmRequest request = new CreateFarmRequest(
            "Trang trại A", 30L, "Địa chỉ A", 1L
        );

        when(farmRepository.existsByUserId(farmerId)).thenReturn(true);

        BusinessException ex = assertThrows(
            BusinessException.class,
            () -> farmService.createMinimalFarmProfile(farmerId, request)
        );
        assertEquals("FARM_ALREADY_EXISTS", ex.getCode());
        verify(farmRepository, never()).save(any());
    }
}
