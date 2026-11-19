package com.nsv.backend.dispute.storage;

import com.nsv.backend.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

@Service
public class LocalPersistentEvidenceStorageService implements EvidenceStorageService {

    private static final Map<String, String> ALLOWED_IMAGE_MIME_TO_EXT = Map.of(
        "image/jpeg", "jpg",
        "image/png", "png",
        "image/webp", "webp"
    );

    private static final Map<String, String> ALLOWED_VIDEO_MIME_TO_EXT = Map.of(
        "video/mp4", "mp4",
        "video/webm", "webm",
        "video/quicktime", "mov"
    );

    private final Path rootLocation;
    private final long maxImageBytes;
    private final long maxVideoBytes;

    public LocalPersistentEvidenceStorageService(
        @Value("${app.storage.evidence-dir:/var/lib/nsv/persistent-storage/evidences}") String evidenceDir,
        @Value("${app.storage.max-image-bytes:10485760}") long maxImageBytes,
        @Value("${app.storage.max-video-bytes:104857600}") long maxVideoBytes
    ) {
        this.rootLocation = Paths.get(evidenceDir).toAbsolutePath().normalize();
        this.maxImageBytes = maxImageBytes;
        this.maxVideoBytes = maxVideoBytes;
        initStorageDirectory();
    }

    private void initStorageDirectory() {
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException ex) {
            throw new IllegalStateException("Không thể khởi tạo thư mục lưu trữ bền vững cho minh chứng: " + rootLocation, ex);
        }
    }

    @Override
    public StoredEvidenceFile store(Long complaintId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "EMPTY_EVIDENCE_FILE", "Tệp minh chứng không được để trống");
        }

        String rawContentType = file.getContentType();
        String contentType = rawContentType != null ? rawContentType.trim().toLowerCase() : "";

        String mediaType;
        String extension;
        if (ALLOWED_IMAGE_MIME_TO_EXT.containsKey(contentType)) {
            mediaType = "IMAGE";
            extension = ALLOWED_IMAGE_MIME_TO_EXT.get(contentType);
            if (file.getSize() > maxImageBytes) {
                throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "IMAGE_FILE_TOO_LARGE",
                    "Dung lượng ảnh minh chứng vượt quá giới hạn cho phép (" + (maxImageBytes / (1024 * 1024)) + "MB)"
                );
            }
        } else if (ALLOWED_VIDEO_MIME_TO_EXT.containsKey(contentType)) {
            mediaType = "VIDEO";
            extension = ALLOWED_VIDEO_MIME_TO_EXT.get(contentType);
            if (file.getSize() > maxVideoBytes) {
                throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "VIDEO_FILE_TOO_LARGE",
                    "Dung lượng video minh chứng vượt quá giới hạn cho phép (" + (maxVideoBytes / (1024 * 1024)) + "MB)"
                );
            }
        } else {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "UNSUPPORTED_EVIDENCE_MEDIA_TYPE",
                "Định dạng tệp không được hỗ trợ. Chỉ chấp nhận ảnh (JPG, PNG, WEBP) hoặc video (MP4, WEBM, MOV)"
            );
        }

        String originalFilename = sanitizeFilename(file.getOriginalFilename(), "evidence." + extension);
        String subFolder = "complaint-" + complaintId;
        String storedFileName = UUID.randomUUID() + "." + extension;
        String storageKey = subFolder + "/" + storedFileName;

        Path destinationFile = resolveSafePath(storageKey);

        try {
            Files.createDirectories(destinationFile.getParent());
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

            long writtenBytes = 0L;
            try (
                InputStream in = file.getInputStream();
                DigestInputStream digestIn = new DigestInputStream(in, sha256);
                OutputStream out = Files.newOutputStream(
                    destinationFile,
                    StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE
                )
            ) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = digestIn.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    writtenBytes += bytesRead;
                }
                out.flush();
            }

            String checksum = HexFormat.of().formatHex(sha256.digest());
            return new StoredEvidenceFile(
                storageKey,
                originalFilename,
                mediaType,
                contentType,
                writtenBytes,
                checksum
            );
        } catch (IOException | NoSuchAlgorithmException ex) {
            deleteQuietly(storageKey);
            throw new BusinessException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "EVIDENCE_STORAGE_FAILURE",
                "Lỗi hệ thống khi ghi tệp minh chứng vào kho lưu trữ bền vững"
            );
        }
    }

    @Override
    public Resource loadAsResource(String storageKey) {
        if (!StringUtils.hasText(storageKey)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "EVIDENCE_FILE_NOT_FOUND", "Không tìm thấy khóa lưu trữ của minh chứng");
        }

        Path filePath = resolveSafePath(storageKey);
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BusinessException(
                    HttpStatus.NOT_FOUND,
                    "EVIDENCE_FILE_MISSING",
                    "Tệp minh chứng không tồn tại hoặc không thể đọc từ kho lưu trữ"
                );
            }
            return resource;
        } catch (MalformedURLException ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_STORAGE_PATH", "Đường dẫn lưu trữ minh chứng không hợp lệ");
        }
    }

    @Override
    public void deleteQuietly(String storageKey) {
        if (!StringUtils.hasText(storageKey)) {
            return;
        }
        try {
            Path filePath = resolveSafePath(storageKey);
            Files.deleteIfExists(filePath);
        } catch (Exception ignored) {
            // Bỏ qua lỗi khi dọn dẹp tệp rollback
        }
    }

    private Path resolveSafePath(String storageKey) {
        Path resolved = this.rootLocation.resolve(storageKey).normalize();
        if (!resolved.startsWith(this.rootLocation) || resolved.equals(this.rootLocation)) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "INVALID_STORAGE_PATH",
                "Phát hiện đường dẫn truy xuất tệp không hợp lệ"
            );
        }
        return resolved;
    }

    private String sanitizeFilename(String rawName, String fallback) {
        if (!StringUtils.hasText(rawName)) {
            return fallback;
        }
        String baseName = Paths.get(rawName).getFileName().toString();
        String cleaned = baseName.replaceAll("[^a-zA-Z0-9._-]", "_");
        return cleaned.isBlank() ? fallback : cleaned;
    }
}
