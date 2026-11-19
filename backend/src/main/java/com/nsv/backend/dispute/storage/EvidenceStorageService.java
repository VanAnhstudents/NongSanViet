package com.nsv.backend.dispute.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface EvidenceStorageService {
    StoredEvidenceFile store(Long complaintId, MultipartFile file);
    Resource loadAsResource(String storageKey);
    void deleteQuietly(String storageKey);
}
