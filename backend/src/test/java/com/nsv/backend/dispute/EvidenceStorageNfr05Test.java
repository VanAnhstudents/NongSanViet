package com.nsv.backend.dispute;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.dispute.dto.ComplaintEvidenceResponse;
import com.nsv.backend.dispute.dto.EvidenceDownloadPayload;
import com.nsv.backend.dispute.entity.Complaint;
import com.nsv.backend.dispute.entity.ComplaintEvidence;
import com.nsv.backend.dispute.repository.ComplaintEvidenceRepository;
import com.nsv.backend.dispute.repository.ComplaintRepository;
import com.nsv.backend.dispute.service.EvidenceAccessServiceImpl;
import com.nsv.backend.dispute.storage.LocalPersistentEvidenceStorageService;
import com.nsv.backend.order.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvidenceStorageNfr05Test {

    @TempDir
    Path persistentVolumeDir;

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private ComplaintEvidenceRepository evidenceRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("TC-STORAGE-01 (1): Minh chứng lưu vào Persistent Storage không bị mất dữ liệu và giữ nguyên SHA-256 khi hệ thống restart")
    void uploadAndSurviveApplicationRestart_DataIntact() throws Exception {
        Long buyerId = 20L;
        Long adminId = 1L;
        Long complaintId = 301L;
        Long orderId = 501L;

        Complaint complaint = Complaint.builder()
            .id(complaintId)
            .orderId(orderId)
            .buyerId(buyerId)
            .status("OPEN")
            .build();

        byte[] videoBinaryContent = "FAKE_MP4_HEADER_AND_EVIDENCE_VIDEO_FRAMES_DATA_2026".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile videoFile = new MockMultipartFile(
            "file",
            "unbox-dong-kiem.mp4",
            "video/mp4",
            videoBinaryContent
        );

        AtomicReference<ComplaintEvidence> persistedEntityRef = new AtomicReference<>();

        when(complaintRepository.findById(complaintId)).thenReturn(Optional.of(complaint));
        when(evidenceRepository.save(any(ComplaintEvidence.class))).thenAnswer(inv -> {
            ComplaintEvidence ev = inv.getArgument(0);
            if (ev.getId() == null) {
                ev.setId(9001L);
            }
            persistedEntityRef.set(ev);
            return ev;
        });

        // === PHASE 1: Trước khi restart — Buyer tải video minh chứng lên ===
        LocalPersistentEvidenceStorageService storageBeforeRestart = new LocalPersistentEvidenceStorageService(
            persistentVolumeDir.toString(),
            10_485_760L,
            104_857_600L
        );
        EvidenceAccessServiceImpl serviceBeforeRestart = new EvidenceAccessServiceImpl(
            complaintRepository,
            evidenceRepository,
            orderRepository,
            storageBeforeRestart,
            jdbcTemplate
        );

        ComplaintEvidenceResponse uploaded = serviceBeforeRestart.uploadEvidence(
            buyerId, "BUYER", complaintId, videoFile, "Video mở hộp kiểm tra hàng"
        );

        assertNotNull(uploaded);
        assertEquals(9001L, uploaded.id());
        assertEquals("VIDEO", uploaded.mediaType());
        assertEquals(videoBinaryContent.length, uploaded.fileSize());
        assertNotNull(uploaded.checksumSha256());

        ComplaintEvidence savedEntity = persistedEntityRef.get();
        assertNotNull(savedEntity.getStorageKey());
        assertTrue(Files.exists(persistentVolumeDir.resolve(savedEntity.getStorageKey())));

        // === PHASE 2: Mô phỏng hệ thống restart (khởi tạo bean storage & service hoàn toàn mới trỏ cùng volume) ===
        LocalPersistentEvidenceStorageService storageAfterRestart = new LocalPersistentEvidenceStorageService(
            persistentVolumeDir.toString(),
            10_485_760L,
            104_857_600L
        );
        EvidenceAccessServiceImpl serviceAfterRestart = new EvidenceAccessServiceImpl(
            complaintRepository,
            evidenceRepository,
            orderRepository,
            storageAfterRestart,
            jdbcTemplate
        );

        when(evidenceRepository.findWithComplaintById(9001L)).thenReturn(Optional.of(savedEntity));

        EvidenceDownloadPayload downloadedAfterRestart = serviceAfterRestart.downloadEvidenceContent(
            adminId, "ADMIN", 9001L
        );

        byte[] readBackBytes = readResource(downloadedAfterRestart.resource());
        assertArrayEquals(videoBinaryContent, readBackBytes, "Dữ liệu tệp sau khi restart phải khớp 100%");
        assertEquals(uploaded.checksumSha256(), downloadedAfterRestart.checksumSha256());
    }

    @Test
    @DisplayName("TC-STORAGE-01 (2a): Chỉ Buyer, Farmer của khiếu nại đó và Admin mới được phép truy cập minh chứng")
    void accessControl_AllowedStakeholders_Succeed() throws Exception {
        Long buyerId = 20L;
        Long farmerId = 10L;
        Long adminId = 1L;
        Long complaintId = 301L;
        Long orderId = 501L;
        Long evidenceId = 9002L;

        LocalPersistentEvidenceStorageService storage = new LocalPersistentEvidenceStorageService(
            persistentVolumeDir.toString(),
            10_485_760L,
            104_857_600L
        );
        EvidenceAccessServiceImpl accessService = new EvidenceAccessServiceImpl(
            complaintRepository,
            evidenceRepository,
            orderRepository,
            storage,
            jdbcTemplate
        );

        Complaint complaint = Complaint.builder()
            .id(complaintId)
            .orderId(orderId)
            .buyerId(buyerId)
            .status("IN_REVIEW")
            .build();

        byte[] imageBytes = "JPEG_IMAGE_BINARY_CONTENT".getBytes(StandardCharsets.UTF_8);
        var storedFile = storage.store(
            complaintId,
            new MockMultipartFile("file", "anh-hong.jpg", "image/jpeg", imageBytes)
        );

        ComplaintEvidence evidence = ComplaintEvidence.builder()
            .id(evidenceId)
            .complaint(complaint)
            .uploadedBy(buyerId)
            .uploaderRole("BUYER")
            .mediaType("IMAGE")
            .mediaUrl("/api/v1/disputes/evidences/" + evidenceId + "/content")
            .storageKey(storedFile.storageKey())
            .originalFilename(storedFile.originalFilename())
            .contentType(storedFile.contentType())
            .fileSize(storedFile.fileSize())
            .checksumSha256(storedFile.checksumSha256())
            .build();

        when(evidenceRepository.findWithComplaintById(evidenceId)).thenReturn(Optional.of(evidence));
        when(orderRepository.existsOrderForFarmer(orderId, farmerId)).thenReturn(true);

        // 1. Buyer chủ khiếu nại truy cập được
        EvidenceDownloadPayload buyerPayload = accessService.downloadEvidenceContent(buyerId, "BUYER", evidenceId);
        assertArrayEquals(imageBytes, readResource(buyerPayload.resource()));

        // 2. Farmer của đơn hàng bị khiếu nại truy cập được
        EvidenceDownloadPayload farmerPayload = accessService.downloadEvidenceContent(farmerId, "FARMER", evidenceId);
        assertArrayEquals(imageBytes, readResource(farmerPayload.resource()));

        // 3. Admin phân xử truy cập được
        EvidenceDownloadPayload adminPayload = accessService.downloadEvidenceContent(adminId, "ADMIN", evidenceId);
        assertArrayEquals(imageBytes, readResource(adminPayload.resource()));
    }

    @Test
    @DisplayName("TC-STORAGE-01 (2b): Chặn Buyer khác và Farmer khác truy cập minh chứng của khiếu nại (403 Forbidden)")
    void accessControl_UnrelatedUsers_ThrowsForbidden() {
        Long ownerBuyerId = 20L;
        Long strangerBuyerId = 999L;
        Long strangerFarmerId = 888L;
        Long complaintId = 301L;
        Long orderId = 501L;
        Long evidenceId = 9003L;

        LocalPersistentEvidenceStorageService storage = new LocalPersistentEvidenceStorageService(
            persistentVolumeDir.toString(),
            10_485_760L,
            104_857_600L
        );
        EvidenceAccessServiceImpl accessService = new EvidenceAccessServiceImpl(
            complaintRepository,
            evidenceRepository,
            orderRepository,
            storage,
            jdbcTemplate
        );

        Complaint complaint = Complaint.builder()
            .id(complaintId)
            .orderId(orderId)
            .buyerId(ownerBuyerId)
            .status("OPEN")
            .build();

        ComplaintEvidence evidence = ComplaintEvidence.builder()
            .id(evidenceId)
            .complaint(complaint)
            .uploadedBy(ownerBuyerId)
            .uploaderRole("BUYER")
            .mediaType("IMAGE")
            .storageKey("complaint-301/test.jpg")
            .build();

        when(evidenceRepository.findWithComplaintById(evidenceId)).thenReturn(Optional.of(evidence));
        when(orderRepository.existsOrderForFarmer(orderId, strangerFarmerId)).thenReturn(false);

        // 1. Buyer khác cố truy cập -> 403 Forbidden
        BusinessException buyerDenied = assertThrows(
            BusinessException.class,
            () -> accessService.downloadEvidenceContent(strangerBuyerId, "BUYER", evidenceId)
        );
        assertEquals(HttpStatus.FORBIDDEN, buyerDenied.getStatus());
        assertEquals("EVIDENCE_ACCESS_DENIED", buyerDenied.getCode());

        // 2. Farmer khác không thuộc đơn hàng cố truy cập -> 403 Forbidden
        BusinessException farmerDenied = assertThrows(
            BusinessException.class,
            () -> accessService.getEvidenceMetadata(strangerFarmerId, "FARMER", evidenceId)
        );
        assertEquals(HttpStatus.FORBIDDEN, farmerDenied.getStatus());
        assertEquals("EVIDENCE_ACCESS_DENIED", farmerDenied.getCode());
    }

    @Test
    @DisplayName("TC-STORAGE-01 (2c): Chặn tệp sai định dạng và tấn công Path Traversal")
    void storageSecurity_InvalidMimeTypeAndPathTraversal_Rejected() {
        LocalPersistentEvidenceStorageService storage = new LocalPersistentEvidenceStorageService(
            persistentVolumeDir.toString(),
            10_485_760L,
            104_857_600L
        );

        MockMultipartFile executableFile = new MockMultipartFile(
            "file",
            "malware.exe",
            "application/x-msdownload",
            "MZ_BINARY".getBytes(StandardCharsets.UTF_8)
        );

        BusinessException badMime = assertThrows(
            BusinessException.class,
            () -> storage.store(301L, executableFile)
        );
        assertEquals(HttpStatus.BAD_REQUEST, badMime.getStatus());
        assertEquals("UNSUPPORTED_EVIDENCE_MEDIA_TYPE", badMime.getCode());

        BusinessException traversal = assertThrows(
            BusinessException.class,
            () -> storage.loadAsResource("../../etc/passwd")
        );
        assertEquals(HttpStatus.BAD_REQUEST, traversal.getStatus());
        assertEquals("INVALID_STORAGE_PATH", traversal.getCode());
    }

    private static byte[] readResource(Resource resource) throws IOException {
        try (InputStream input = resource.getInputStream()) {
            return input.readAllBytes();
        }
    }
}
