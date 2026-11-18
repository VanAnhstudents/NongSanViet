package com.nsv.backend.dispute.service;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.dispute.dto.ComplaintEvidenceResponse;
import com.nsv.backend.dispute.dto.EvidenceDownloadPayload;
import com.nsv.backend.dispute.entity.Complaint;
import com.nsv.backend.dispute.entity.ComplaintEvidence;
import com.nsv.backend.dispute.repository.ComplaintEvidenceRepository;
import com.nsv.backend.dispute.repository.ComplaintRepository;
import com.nsv.backend.dispute.storage.EvidenceStorageService;
import com.nsv.backend.dispute.storage.StoredEvidenceFile;
import com.nsv.backend.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class EvidenceAccessServiceImpl implements EvidenceAccessService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintEvidenceRepository evidenceRepository;
    private final OrderRepository orderRepository;
    private final EvidenceStorageService storageService;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public ComplaintEvidenceResponse uploadEvidence(
            Long actorId,
            String role,
            Long complaintId,
            MultipartFile file,
            String caption
    ) {
        Complaint complaint = complaintRepository.findById(complaintId)
            .orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND,
                "COMPLAINT_NOT_FOUND",
                "Không tìm thấy hồ sơ khiếu nại"
            ));

        // Chỉ các bên liên quan trực tiếp đến khiếu nại mới được tải lên minh chứng
        verifyStakeholderAccess(actorId, role, complaint);

        if ("RESOLVED_REFUND".equals(complaint.getStatus()) || "RESOLVED_REJECTED".equals(complaint.getStatus())) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "COMPLAINT_ALREADY_RESOLVED",
                "Không thể bổ sung minh chứng sau khi tranh chấp đã được Admin phân xử"
            );
        }

        StoredEvidenceFile storedFile = storageService.store(complaintId, file);

        try {
            ComplaintEvidence evidence = ComplaintEvidence.builder()
                .complaint(complaint)
                .uploadedBy(actorId)
                .uploaderRole(role)
                .mediaType(storedFile.mediaType())
                .mediaUrl("/api/v1/disputes/evidences/pending/content")
                .storageKey(storedFile.storageKey())
                .originalFilename(storedFile.originalFilename())
                .contentType(storedFile.contentType())
                .fileSize(storedFile.fileSize())
                .checksumSha256(storedFile.checksumSha256())
                .caption(StringUtils.hasText(caption) ? caption.trim() : null)
                .build();

            ComplaintEvidence saved = evidenceRepository.save(evidence);
            saved.setMediaUrl("/api/v1/disputes/evidences/" + saved.getId() + "/content");
            saved = evidenceRepository.save(saved);

            recordAuditLog(actorId, "UPLOAD_DISPUTE_EVIDENCE", "complaint_evidences", saved.getId());
            return ComplaintEvidenceResponse.from(saved);
        } catch (RuntimeException ex) {
            storageService.deleteQuietly(storedFile.storageKey());
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ComplaintEvidenceResponse getEvidenceMetadata(Long actorId, String role, Long evidenceId) {
        ComplaintEvidence evidence = evidenceRepository.findWithComplaintById(evidenceId)
            .orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND,
                "EVIDENCE_NOT_FOUND",
                "Không tìm thấy minh chứng khiếu nại"
            ));

        verifyStakeholderAccess(actorId, role, evidence.getComplaint());
        return ComplaintEvidenceResponse.from(evidence);
    }

    @Override
    @Transactional
    public EvidenceDownloadPayload downloadEvidenceContent(Long actorId, String role, Long evidenceId) {
        ComplaintEvidence evidence = evidenceRepository.findWithComplaintById(evidenceId)
            .orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND,
                "EVIDENCE_NOT_FOUND",
                "Không tìm thấy minh chứng khiếu nại"
            ));

        // Chặn mọi truy cập ngoài Buyer của khiếu nại, Farmer của đơn hàng đó và Admin
        verifyStakeholderAccess(actorId, role, evidence.getComplaint());

        Resource resource = storageService.loadAsResource(evidence.getStorageKey());
        recordAuditLog(actorId, "ACCESS_DISPUTE_EVIDENCE", "complaint_evidences", evidence.getId());

        return new EvidenceDownloadPayload(
            resource,
            evidence.getOriginalFilename() != null ? evidence.getOriginalFilename() : "evidence-" + evidence.getId(),
            evidence.getContentType() != null ? evidence.getContentType() : "application/octet-stream",
            evidence.getFileSize() != null ? evidence.getFileSize() : 0L,
            evidence.getChecksumSha256()
        );
    }

    private void verifyStakeholderAccess(Long actorId, String role, Complaint complaint) {
        if ("ADMIN".equals(role)) {
            return;
        }
        if ("BUYER".equals(role) && complaint.getBuyerId().equals(actorId)) {
            return;
        }
        if ("FARMER".equals(role) && orderRepository.existsOrderForFarmer(complaint.getOrderId(), actorId)) {
            return;
        }

        throw new BusinessException(
            HttpStatus.FORBIDDEN,
            "EVIDENCE_ACCESS_DENIED",
            "Bạn không thuộc các bên liên quan (Buyer, Farmer, Admin) của khiếu nại này nên không thể truy cập minh chứng"
        );
    }

    private void recordAuditLog(Long actorId, String action, String entityType, Long entityId) {
        jdbcTemplate.update(
            "INSERT INTO audit_logs (actor_id, action, entity_type, entity_id) VALUES (?, ?, ?, ?)",
            actorId,
            action,
            entityType,
            entityId
        );
    }
}
