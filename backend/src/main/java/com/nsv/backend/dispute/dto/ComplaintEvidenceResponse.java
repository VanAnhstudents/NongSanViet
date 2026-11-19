package com.nsv.backend.dispute.dto;

import com.nsv.backend.dispute.entity.ComplaintEvidence;

import java.time.OffsetDateTime;

public record ComplaintEvidenceResponse(
    Long id,
    Long complaintId,
    Long uploadedBy,
    String uploaderRole,
    String mediaType,
    String mediaUrl,
    String originalFilename,
    String contentType,
    Long fileSize,
    String checksumSha256,
    String caption,
    OffsetDateTime createdAt
) {
    public static ComplaintEvidenceResponse from(ComplaintEvidence entity) {
        return new ComplaintEvidenceResponse(
            entity.getId(),
            entity.getComplaint() != null ? entity.getComplaint().getId() : null,
            entity.getUploadedBy(),
            entity.getUploaderRole(),
            entity.getMediaType(),
            entity.getMediaUrl(),
            entity.getOriginalFilename(),
            entity.getContentType(),
            entity.getFileSize(),
            entity.getChecksumSha256(),
            entity.getCaption(),
            entity.getCreatedAt()
        );
    }
}
