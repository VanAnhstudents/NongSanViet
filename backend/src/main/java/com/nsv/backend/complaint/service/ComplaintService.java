package com.nsv.backend.complaint.service;

import com.nsv.backend.admin.service.AuditLogService;
import com.nsv.backend.common.exception.ApiException;
import com.nsv.backend.complaint.dto.ComplaintSlaResponse;
import com.nsv.backend.complaint.dto.EscalatedComplaintItem;
import com.nsv.backend.complaint.dto.FarmerComplaintItem;
import com.nsv.backend.dispute.entity.Complaint;
import com.nsv.backend.complaint.repository.PbiComplaintRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * PBI-30: Farmer phản hồi khiếu nại trong SLA 24h; quá hạn thì tự động leo thang lên Admin.
 *
 * Bảng complaints không có cột thời điểm phản hồi / leo thang nên hai sự kiện này được ghi vào audit_logs
 * (action COMPLAINT_RESPONDED, COMPLAINT_ESCALATED). Hạn SLA tính từ complaints.created_at.
 * Cột status không bị đổi ở PBI này (danh sách CHECK của status chưa được xác nhận).
 */
@Service
public class ComplaintService {

    public static final String ENTITY = "COMPLAINT";
    public static final String ACTION_RESPONDED = "COMPLAINT_RESPONDED";
    public static final String ACTION_ESCALATED = "COMPLAINT_ESCALATED";

    private final PbiComplaintRepository complaintRepository;
    private final AuditLogService auditLogService;
    private final Clock clock;
    private final Duration sla;

    public ComplaintService(PbiComplaintRepository complaintRepository, AuditLogService auditLogService, Clock clock,
            @Value("${app.complaint.sla-hours:24}") long slaHours) {
        this.complaintRepository = complaintRepository;
        this.auditLogService = auditLogService;
        this.clock = clock;
        this.sla = Duration.ofHours(slaHours);
    }

    @Transactional(readOnly = true)
    public List<FarmerComplaintItem> listForFarmer(Long farmerId) {
        Map<Long, OffsetDateTime> escalated = auditLogService.entityTimesByAction(ACTION_ESCALATED, ENTITY);
        return complaintRepository.findAllOfFarmer(farmerId).stream()
                .map(c -> new FarmerComplaintItem(c.getId(), c.getOrderId(), c.getReason(),
                        c.getCreatedAt().toInstant(),
                        deadline(c), c.getFarmerResponse() != null, escalated.containsKey(c.getId()),
                        c.getFarmerResponse()))
                .toList();
    }

    @Transactional
    public ComplaintSlaResponse respond(Long farmerId, Long complaintId, String text) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .filter(c -> complaintRepository.existsOrderOfFarmer(c.getOrderId(), farmerId))
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy khiếu nại của bạn"));
        if (complaint.getAdminDecision() != null) {
            throw invalidState("Khiếu nại đã có quyết định của Admin");
        }
        if (complaint.getFarmerResponse() != null) {
            throw invalidState("Khiếu nại này đã được phản hồi");
        }
        if (auditLogService.exists(ACTION_ESCALATED, ENTITY, complaintId)) {
            throw invalidState("Khiếu nại đã được chuyển cấp Admin do quá hạn phản hồi");
        }

        Instant now = Instant.now(clock);
        complaint.setFarmerResponse(text.trim());
        complaintRepository.save(complaint);
        auditLogService.record(farmerId, ACTION_RESPONDED, ENTITY, complaintId);

        Instant deadline = deadline(complaint);
        return new ComplaintSlaResponse(complaintId, complaint.getFarmerResponse(),
                complaint.getCreatedAt().toInstant(),
                deadline, now, !now.isAfter(deadline));
    }

    /** Chuyển cấp lên Admin mọi khiếu nại quá hạn SLA mà Farmer chưa phản hồi. Trả về số khiếu nại vừa chuyển. */
    @Transactional
    public int escalateOverdue() {
        OffsetDateTime limit = OffsetDateTime.now(clock).minus(sla);
        int escalated = 0;
        for (Complaint c : complaintRepository
                .findByFarmerResponseIsNullAndAdminDecisionIsNullAndCreatedAtLessThanEqual(limit)) {
            if (auditLogService.exists(ACTION_ESCALATED, ENTITY, c.getId())) {
                continue;
            }
            auditLogService.record(null, ACTION_ESCALATED, ENTITY, c.getId());
            escalated++;
        }
        return escalated;
    }

    @Transactional(readOnly = true)
    public List<EscalatedComplaintItem> listEscalated() {
        Map<Long, OffsetDateTime> times = auditLogService.entityTimesByAction(ACTION_ESCALATED, ENTITY);
        return complaintRepository.findAllById(times.keySet()).stream()
                .sorted(Comparator.comparing(Complaint::getId).reversed())
                .map(c -> new EscalatedComplaintItem(c.getId(), c.getOrderId(), c.getReason(),
                        c.getCreatedAt().toInstant(),
                        times.get(c.getId()) != null ? times.get(c.getId()).toInstant() : null, c.getFarmerResponse(), c.getAdminDecision()))
                .toList();
    }

    private Instant deadline(Complaint c) {
        return c.getCreatedAt().toInstant().plus(sla);
    }

    private ApiException invalidState(String message) {
        return new ApiException(HttpStatus.CONFLICT, "INVALID_STATE", message, null);
    }
}
