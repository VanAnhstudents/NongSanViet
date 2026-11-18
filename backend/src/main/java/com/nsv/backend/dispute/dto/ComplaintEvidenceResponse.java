package com.nsv.backend.dispute.dto;

import com.nsv.backend.dispute.entity.ComplaintEvidence;

import java.time.OffsetDateTime;

public record ComplaintEvidenceResponse(
    Long id,
    Long uploadedBy,
    String uploaderRole,
    String mediaType,
    String mediaUrl,
    String caption,
    OffsetDateTime createdAt
) {
    public static ComplaintEvidenceResponse from(ComplaintEvidence entity) {
        return new ComplaintEvidenceResponse(
            entity.getId(),
            entity.getUploadedBy(),
            entity.getUploaderRole(),
            entity.getMediaType(),
            entity.getMediaUrl(),
            entity.getCaption(),
            entity.getCreatedAt()
        );
    }
}
