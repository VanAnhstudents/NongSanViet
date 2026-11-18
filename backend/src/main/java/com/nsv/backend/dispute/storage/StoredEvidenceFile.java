package com.nsv.backend.dispute.storage;

public record StoredEvidenceFile(
    String storageKey,
    String originalFilename,
    String mediaType,
    String contentType,
    long fileSize,
    String checksumSha256
) {}
