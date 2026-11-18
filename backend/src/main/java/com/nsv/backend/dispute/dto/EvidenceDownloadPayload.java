package com.nsv.backend.dispute.dto;

import org.springframework.core.io.Resource;

public record EvidenceDownloadPayload(
    Resource resource,
    String originalFilename,
    String contentType,
    long fileSize,
    String checksumSha256
) {}
