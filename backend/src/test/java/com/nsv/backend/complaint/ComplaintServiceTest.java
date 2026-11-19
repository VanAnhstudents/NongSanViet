package com.nsv.backend.complaint;

import com.nsv.backend.admin.service.AuditLogService;
import com.nsv.backend.common.exception.ApiException;
import com.nsv.backend.complaint.dto.ComplaintSlaResponse;
import com.nsv.backend.complaint.repository.PbiComplaintRepository;
import com.nsv.backend.complaint.service.ComplaintService;
import com.nsv.backend.dispute.entity.Complaint;
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
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ComplaintServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-17T03:00:00Z");
    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final OffsetDateTime NOW_AT = OffsetDateTime.ofInstant(NOW, ZONE);
    private static final long FARMER_ID = 10L;

    @Mock
    private PbiComplaintRepository complaintRepository;

    @Mock
    private AuditLogService auditLogService;

    private ComplaintService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, ZONE);
        service = new ComplaintService(complaintRepository, auditLogService, clock, 24);
        when(complaintRepository.existsOrderOfFarmer(100L, FARMER_ID)).thenReturn(true);
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Complaint complaintCreatedHoursAgo(long hours) {
        return Complaint.builder().id(1L).orderId(100L).reason("Cà chua bị dập").status("OPEN")
                .createdAt(NOW_AT.minusHours(hours)).build();
    }

    @Test
    @DisplayName("TC-COMPLAIN-02: Farmer phản hồi trong 24h -> ghi nhận thời điểm phản hồi, đạt SLA")
    void tc02_respondWithinSla() {
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(complaintCreatedHoursAgo(5)));

        ComplaintSlaResponse result = service.respond(FARMER_ID, 1L, "  Shop xin lỗi, sẽ đổi hàng  ");

        assertThat(result.withinSla()).isTrue();
        assertThat(result.respondedAt()).isEqualTo(NOW);
        assertThat(result.slaDeadline()).isEqualTo(NOW.plusSeconds(19 * 3600));
        assertThat(result.farmerResponse()).isEqualTo("Shop xin lỗi, sẽ đổi hàng");
        verify(auditLogService).record(FARMER_ID, "COMPLAINT_RESPONDED", "COMPLAINT", 1L);
    }

    @Test
    @DisplayName("TC-COMPLAIN-02: Farmer phản hồi sau 24h (chưa bị chuyển cấp) -> vẫn lưu nhưng ghi nhận trễ SLA")
    void tc02_respondAfterSlaIsFlagged() {
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(complaintCreatedHoursAgo(30)));

        ComplaintSlaResponse result = service.respond(FARMER_ID, 1L, "Xin lỗi vì phản hồi trễ");

        assertThat(result.withinSla()).isFalse();
        verify(auditLogService).record(FARMER_ID, "COMPLAINT_RESPONDED", "COMPLAINT", 1L);
    }

    @Test
    @DisplayName("TC-COMPLAIN-02: Quá 24h không phản hồi -> hệ thống chuyển cấp lên Admin")
    void tc02_escalatesOverdueComplaint() {
        when(complaintRepository.findByFarmerResponseIsNullAndAdminDecisionIsNullAndCreatedAtLessThanEqual(
                eq(NOW_AT.minusHours(24)))).thenReturn(List.of(complaintCreatedHoursAgo(25)));
        when(auditLogService.exists("COMPLAINT_ESCALATED", "COMPLAINT", 1L)).thenReturn(false);

        int escalated = service.escalateOverdue();

        assertThat(escalated).isEqualTo(1);
        verify(auditLogService).record(null, "COMPLAINT_ESCALATED", "COMPLAINT", 1L);
    }

    @Test
    @DisplayName("TC-COMPLAIN-02: Khiếu nại đã chuyển cấp rồi thì không chuyển cấp lần nữa")
    void tc02_doesNotEscalateTwice() {
        when(complaintRepository.findByFarmerResponseIsNullAndAdminDecisionIsNullAndCreatedAtLessThanEqual(any()))
                .thenReturn(List.of(complaintCreatedHoursAgo(40)));
        when(auditLogService.exists("COMPLAINT_ESCALATED", "COMPLAINT", 1L)).thenReturn(true);

        int escalated = service.escalateOverdue();

        assertThat(escalated).isZero();
        verify(auditLogService, never()).record(any(), anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("TC-COMPLAIN-02: Khiếu nại đã chuyển cấp Admin thì Farmer không phản hồi được nữa")
    void tc02_cannotRespondAfterEscalation() {
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(complaintCreatedHoursAgo(30)));
        when(auditLogService.exists("COMPLAINT_ESCALATED", "COMPLAINT", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.respond(FARMER_ID, 1L, "Phản hồi muộn"))
                .isInstanceOf(ApiException.class);

        verify(complaintRepository, never()).save(any(Complaint.class));
    }
}
