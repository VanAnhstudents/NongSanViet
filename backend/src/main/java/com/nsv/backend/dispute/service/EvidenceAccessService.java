package com.nsv.backend.dispute.service;

import com.nsv.backend.dispute.dto.ComplaintEvidenceResponse;
import com.nsv.backend.dispute.dto.EvidenceDownloadPayload;
import org.springframework.web.multipart.MultipartFile;

public interface EvidenceAccessService {
    ComplaintEvidenceResponse uploadEvidence(
        Long actorId,
        String role,
        Long complaintId,
        MultipartFile file,
        String caption
    );

    ComplaintEvidenceResponse getEvidenceMetadata(Long actorId, String role, Long evidenceId);

    EvidenceDownloadPayload downloadEvidenceContent(Long actorId, String role, Long evidenceId);
}
