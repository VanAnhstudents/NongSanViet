package com.nsv.backend.dispute.controller;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.dispute.dto.ComplaintEvidenceResponse;
import com.nsv.backend.dispute.dto.EvidenceDownloadPayload;
import com.nsv.backend.dispute.service.EvidenceAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/disputes")
@RequiredArgsConstructor
public class EvidenceStorageController {

    private final EvidenceAccessService evidenceAccessService;

    // Tải lên ảnh/video minh chứng vào kho lưu trữ bền vững (Persistent Storage)
    @PostMapping(value = "/{complaintId}/evidences", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('BUYER', 'FARMER', 'ADMIN')")
    public ResponseEntity<ComplaintEvidenceResponse> uploadEvidence(
            Authentication authentication,
            @PathVariable Long complaintId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "caption", required = false) String caption
    ) {
        Long actorId = extractUserId(authentication);
        String role = extractRole(authentication);
        ComplaintEvidenceResponse response = evidenceAccessService.uploadEvidence(
            actorId, role, complaintId, file, caption
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Xem thông tin metadata của minh chứng (chỉ Buyer, Farmer của khiếu nại đó và Admin)
    @GetMapping("/evidences/{evidenceId}")
    @PreAuthorize("hasAnyRole('BUYER', 'FARMER', 'ADMIN')")
    public ResponseEntity<ComplaintEvidenceResponse> getEvidenceMetadata(
            Authentication authentication,
            @PathVariable Long evidenceId
    ) {
        Long actorId = extractUserId(authentication);
        String role = extractRole(authentication);
        return ResponseEntity.ok(evidenceAccessService.getEvidenceMetadata(actorId, role, evidenceId));
    }

    // Truy xuất/tải luồng nhị phân ảnh/video minh chứng an toàn có kiểm soát quyền
    @GetMapping("/evidences/{evidenceId}/content")
    @PreAuthorize("hasAnyRole('BUYER', 'FARMER', 'ADMIN')")
    public ResponseEntity<Resource> downloadEvidenceContent(
            Authentication authentication,
            @PathVariable Long evidenceId
    ) {
        Long actorId = extractUserId(authentication);
        String role = extractRole(authentication);
        EvidenceDownloadPayload payload = evidenceAccessService.downloadEvidenceContent(actorId, role, evidenceId);

        ContentDisposition disposition = ContentDisposition.inline()
            .filename(payload.originalFilename(), StandardCharsets.UTF_8)
            .build();

        ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(payload.contentType()))
            .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
            .header(HttpHeaders.CACHE_CONTROL, "private, no-store, max-age=0");

        if (payload.fileSize() > 0) {
            builder.contentLength(payload.fileSize());
        }
        if (payload.checksumSha256() != null) {
            builder.header("X-Checksum-Sha256", payload.checksumSha256());
        }

        return builder.body(payload.resource());
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Chưa xác thực người dùng");
        }
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException ex) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN_SUBJECT", "Thông tin định danh không hợp lệ");
        }
    }

    private String extractRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(auth -> auth.startsWith("ROLE_"))
            .map(auth -> auth.substring(5))
            .findFirst()
            .orElse("");
    }
}
