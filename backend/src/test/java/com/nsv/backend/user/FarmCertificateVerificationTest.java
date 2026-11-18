package com.nsv.backend.user;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.user.dto.FarmCertificateResponse;
import com.nsv.backend.user.dto.FarmProfileResponse;
import com.nsv.backend.user.dto.ReviewCertificateRequest;
import com.nsv.backend.user.entity.Farm;
import com.nsv.backend.user.entity.FarmCertificate;
import com.nsv.backend.user.repository.FarmCertificateRepository;
import com.nsv.backend.user.repository.FarmRepository;
import com.nsv.backend.user.service.FarmCertificateServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FarmCertificateVerificationTest {

    @Mock
    private FarmRepository farmRepository;

    @Mock
    private FarmCertificateRepository certificateRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private FarmCertificateServiceImpl certificateService;

    @SuppressWarnings("unchecked")
    private void mockSingleStringQuery(String prefix, String returnValue) {
        when(jdbcTemplate.query(
            startsWith(prefix),
            any(PreparedStatementSetter.class),
            any(ResultSetExtractor.class)
        )).thenReturn(returnValue);
    }

    @Test
    @DisplayName("TC-VERIFY-01 (1a): Admin xem danh sách chứng nhận Farmer nộp và phê duyệt -> cập nhật VERIFIED và thông báo")
    void adminListsAndApprovesCertificate_UpdatesFarmStatusAndNotifiesFarmer() {
        Long adminId = 1L;
        Long farmerId = 10L;
        Long farmId = 5L;
        Long certId = 101L;

        Farm farm = Farm.builder()
            .id(farmId)
            .userId(farmerId)
            .provinceId(30L)
            .mainCategoryId(2L)
            .farmName("Trang trại Sinh thái Thanh Sơn")
            .addressLine("Xã Thanh Sơn, Thanh Hà, Hải Phòng")
            .certificateStatus("PENDING")
            .build();

        FarmCertificate pendingCert = FarmCertificate.builder()
            .id(certId)
            .farmId(farmId)
            .certificateType("VIETGAP")
            .certificateNumber("VG-2026-8899")
            .issuedBy("Trung tâm Chất lượng Nông lâm thủy sản")
            .issueDate(LocalDate.of(2026, 1, 15))
            .expiryDate(LocalDate.of(2028, 1, 15))
            .fileUrl("https://cdn.nongsanviet.vn/certs/vietgap-thanhson.pdf")
            .status("PENDING")
            .build();

        when(certificateRepository.findAllByStatusOrderByCreatedAtDesc("PENDING")).thenReturn(List.of(pendingCert));
        when(farmRepository.findAllById(Set.of(farmId))).thenReturn(List.of(farm));
        mockSingleStringQuery("SELECT full_name FROM users", "Nguyễn Văn An");

        // 1. Admin xem danh sách chứng nhận chờ duyệt
        List<FarmCertificateResponse> pendingList = certificateService.getCertificatesForAdmin("PENDING");
        assertEquals(1, pendingList.size());
        assertEquals(certId, pendingList.get(0).id());
        assertEquals("VIETGAP", pendingList.get(0).certificateType());
        assertEquals("https://cdn.nongsanviet.vn/certs/vietgap-thanhson.pdf", pendingList.get(0).fileUrl());
        assertEquals("Trang trại Sinh thái Thanh Sơn", pendingList.get(0).farmName());

        // 2. Admin phê duyệt chứng nhận
        when(certificateRepository.findById(certId)).thenReturn(Optional.of(pendingCert));
        when(certificateRepository.save(any(FarmCertificate.class))).thenAnswer(inv -> inv.getArgument(0));
        when(farmRepository.findById(farmId)).thenReturn(Optional.of(farm));
        when(certificateRepository.findAllByFarmIdOrderByCreatedAtDesc(farmId)).thenReturn(List.of(pendingCert));
        when(farmRepository.save(any(Farm.class))).thenAnswer(inv -> inv.getArgument(0));

        FarmCertificateResponse reviewed = certificateService.reviewCertificate(
            adminId,
            certId,
            new ReviewCertificateRequest("APPROVED", null)
        );

        assertEquals("APPROVED", reviewed.status());
        assertEquals(adminId, reviewed.reviewedBy());
        assertNotNull(reviewed.reviewedAt());
        assertEquals("VERIFIED", farm.getCertificateStatus());
        assertEquals("https://cdn.nongsanviet.vn/certs/vietgap-thanhson.pdf", farm.getCertificateUrl());

        verify(jdbcTemplate, times(1)).update(startsWith("INSERT INTO notifications"), eq(farmerId), anyString());
        verify(jdbcTemplate, times(1)).update(
            startsWith("INSERT INTO audit_logs"),
            eq(adminId),
            eq("REVIEW_FARM_CERTIFICATE_APPROVED"),
            eq("farm_certificates"),
            eq(certId)
        );
    }

    @Test
    @DisplayName("TC-VERIFY-01 (1b): Admin từ chối chứng nhận không hợp lệ -> bắt buộc lý do từ chối và không gắn VERIFIED")
    void adminRejectsCertificate_RequiresReasonAndDoesNotVerifyFarm() {
        Long adminId = 1L;
        Long farmerId = 10L;
        Long farmId = 5L;
        Long certId = 102L;

        Farm farm = Farm.builder()
            .id(farmId)
            .userId(farmerId)
            .farmName("Trang trại Xanh")
            .certificateStatus("PENDING")
            .build();

        FarmCertificate pendingCert = FarmCertificate.builder()
            .id(certId)
            .farmId(farmId)
            .certificateType("ORGANIC")
            .fileUrl("https://cdn.nongsanviet.vn/certs/blurry-image.jpg")
            .status("PENDING")
            .build();

        when(certificateRepository.findById(certId)).thenReturn(Optional.of(pendingCert));

        // Thiếu lý do từ chối -> 400 Bad Request
        BusinessException missingReason = assertThrows(
            BusinessException.class,
            () -> certificateService.reviewCertificate(adminId, certId, new ReviewCertificateRequest("REJECTED", "  "))
        );
        assertEquals(HttpStatus.BAD_REQUEST, missingReason.getStatus());
        assertEquals("REJECTION_REASON_REQUIRED", missingReason.getCode());

        // Từ chối kèm lý do hợp lệ
        when(certificateRepository.save(any(FarmCertificate.class))).thenAnswer(inv -> inv.getArgument(0));
        when(farmRepository.findById(farmId)).thenReturn(Optional.of(farm));
        when(certificateRepository.findAllByFarmIdOrderByCreatedAtDesc(farmId)).thenReturn(List.of(pendingCert));
        when(farmRepository.save(any(Farm.class))).thenAnswer(inv -> inv.getArgument(0));
        mockSingleStringQuery("SELECT full_name FROM users", "Trần Thị Bình");

        FarmCertificateResponse rejected = certificateService.reviewCertificate(
            adminId,
            certId,
            new ReviewCertificateRequest("REJECTED", "Ảnh chụp mờ, không đọc được số hiệu và dấu mộc cơ quan cấp")
        );

        assertEquals("REJECTED", rejected.status());
        assertEquals("Ảnh chụp mờ, không đọc được số hiệu và dấu mộc cơ quan cấp", rejected.rejectionReason());
        assertEquals("REJECTED", farm.getCertificateStatus());
    }

    @Test
    @DisplayName("TC-VERIFY-01 (2): Hồ sơ trang trại hiển thị đầy đủ huy hiệu (badge) xác minh cho các chứng nhận đã duyệt")
    void getFarmProfileWithBadges_DisplaysVerifiedBadgesForApprovedCertificates() {
        Long farmId = 5L;
        Long farmerId = 10L;

        Farm farm = Farm.builder()
            .id(farmId)
            .userId(farmerId)
            .provinceId(30L)
            .mainCategoryId(2L)
            .farmName("Trang trại Sinh thái Thanh Sơn")
            .description("Chuyên canh vải thiều và ổi bo đạt chuẩn VietGAP & Hữu cơ")
            .addressLine("Xã Thanh Sơn, Thanh Hà, Hải Phòng")
            .certificateStatus("VERIFIED")
            .build();

        FarmCertificate vietgap = FarmCertificate.builder()
            .id(101L)
            .farmId(farmId)
            .certificateType("VIETGAP")
            .certificateNumber("VG-2026-8899")
            .issuedBy("Trung tâm Chất lượng Nông lâm thủy sản")
            .expiryDate(LocalDate.of(2028, 1, 15))
            .status("APPROVED")
            .reviewedAt(OffsetDateTime.now())
            .build();

        FarmCertificate organic = FarmCertificate.builder()
            .id(103L)
            .farmId(farmId)
            .certificateType("ORGANIC")
            .certificateNumber("ORG-VN-2026-112")
            .issuedBy("Tổ chức Chứng nhận Hữu cơ Việt Nam")
            .expiryDate(LocalDate.of(2027, 12, 31))
            .status("APPROVED")
            .reviewedAt(OffsetDateTime.now())
            .build();

        when(farmRepository.findById(farmId)).thenReturn(Optional.of(farm));
        when(certificateRepository.findAllByFarmIdAndStatusOrderByReviewedAtDesc(farmId, "APPROVED"))
            .thenReturn(List.of(vietgap, organic));
        mockSingleStringQuery("SELECT full_name FROM users", "Nguyễn Văn An");
        mockSingleStringQuery("SELECT name FROM provinces", "Hải Phòng");
        mockSingleStringQuery("SELECT name FROM categories", "Trái cây theo mùa");

        FarmProfileResponse profile = certificateService.getFarmProfileWithBadges(farmId);

        assertTrue(profile.verified());
        assertEquals("VERIFIED", profile.certificateStatus());
        assertEquals(2, profile.verifiedBadgeCount());
        assertEquals("Chứng nhận VietGAP", profile.verifiedBadges().get(0).badgeLabel());
        assertEquals("VG-2026-8899", profile.verifiedBadges().get(0).certificateNumber());
        assertEquals("Chứng nhận Hữu cơ", profile.verifiedBadges().get(1).badgeLabel());
        assertEquals("ORG-VN-2026-112", profile.verifiedBadges().get(1).certificateNumber());
    }
}
