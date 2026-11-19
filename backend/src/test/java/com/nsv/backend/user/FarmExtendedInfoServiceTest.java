package com.nsv.backend.user;

import com.nsv.backend.user.dto.FarmExtendedInfoRequest;
import com.nsv.backend.user.dto.FarmExtendedInfoResponse;
import com.nsv.backend.user.entity.Farm;
import com.nsv.backend.user.repository.FarmRepository;
import com.nsv.backend.user.service.FarmExtendedInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FarmExtendedInfoServiceTest {

    private static final long USER_ID = 10L;

    @Mock
    private FarmRepository farmRepository;

    private FarmExtendedInfoService service;

    @BeforeEach
    void setUp() {
        service = new FarmExtendedInfoService(farmRepository);
        Farm farm = new Farm();
        farm.setId(1L);
        farm.setUserId(USER_ID);
        farm.setFarmName("Trang trại Bác Ba");
        when(farmRepository.findByUserId(USER_ID)).thenReturn(Optional.of(farm));
        when(farmRepository.save(any(Farm.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    @DisplayName("TC-FARM-02: Farmer thêm thông tin mở rộng (chứng nhận) -> lưu thành công")
    void tc02_saveExtendedInfo() {
        FarmExtendedInfoResponse result = service.update(USER_ID,
                new FarmExtendedInfoRequest("https://example.com/vietgap.pdf"));

        assertThat(result.certificateUrl()).isEqualTo("https://example.com/vietgap.pdf");
        assertThat(result.certificateStatus()).isEqualTo("PENDING");
        ArgumentCaptor<Farm> saved = ArgumentCaptor.forClass(Farm.class);
        verify(farmRepository).save(saved.capture());
        assertThat(saved.getValue().getCertificateUrl()).isEqualTo("https://example.com/vietgap.pdf");
    }

    @Test
    @DisplayName("TC-FARM-02: Để trống các trường mở rộng -> vẫn lưu được, không báo lỗi")
    void tc02_blankFieldsStillSave() {
        FarmExtendedInfoResponse result = service.update(USER_ID, new FarmExtendedInfoRequest("  "));

        assertThat(result.certificateUrl()).isNull();
        assertThat(result.certificateStatus()).isEqualTo("NONE");
        verify(farmRepository).save(any(Farm.class));
    }
}
