package com.nsv.backend.trace.service;

import com.nsv.backend.common.exception.ApiException;
import com.nsv.backend.common.exception.FieldErrorDetail;
import com.nsv.backend.trace.dto.IntegrityIssue;
import com.nsv.backend.trace.dto.IntegrityReport;
import com.nsv.backend.trace.repository.FarmingLogIntegrityRepository;
import com.nsv.backend.trace.repository.LogRecord;
import com.nsv.backend.trace.repository.SealRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * PBI-35: toàn vẹn nhật ký canh tác và mã QR.
 * "Niêm phong" một dòng nhật ký = ghi chữ ký HMAC-SHA256 (khóa bí mật của hệ thống) của toàn bộ nội dung dòng đó
 * cùng mã truy xuất của lô. Sau khi niêm phong: (1) không sửa/xóa qua ứng dụng được (assertEditable);
 * (2) mọi chỉnh sửa trực tiếp trong DB đều bị phát hiện khi đối chiếu (verify); (3) chỉ có thể bổ sung dòng mới.
 */
@Service
public class FarmingLogIntegrityService {

    /** Độ dài chữ ký lưu lại (base64url): vừa đủ để action trong audit_logs không vượt 50 ký tự. */
    static final int MAC_LENGTH = 34;

    private final FarmingLogIntegrityRepository repository;
    private final byte[] secret;

    public FarmingLogIntegrityService(FarmingLogIntegrityRepository repository,
            @Value("${app.integrity.secret:${JWT_SECRET:}}") String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("Thiếu khóa niêm phong: đặt app.integrity.secret (INTEGRITY_SECRET) hoặc JWT_SECRET");
        }
        this.repository = repository;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    /** Niêm phong mọi dòng nhật ký chưa niêm phong. Trả về số dòng vừa niêm phong. */
    @Transactional
    public int sealUnsealedLogs() {
        List<LogRecord> unsealed = repository.findUnsealedLogs();
        for (LogRecord log : unsealed) {
            repository.saveSeal(log.id(), log.batchId(), mac(log));
        }
        return unsealed.size();
    }

    /** Gọi ở đầu mọi thao tác sửa/xóa nhật ký: dòng đã niêm phong thì từ chối (409). */
    @Transactional(readOnly = true)
    public void assertEditable(Long logId) {
        if (repository.isSealed(logId)) {
            throw new ApiException(HttpStatus.CONFLICT, "LOG_SEALED",
                    "Nhật ký đã công khai nên không thể sửa hoặc xóa; chỉ có thể bổ sung mục mới", null);
        }
    }

    /** Đối chiếu dữ liệu gắn với mã QR (mã truy xuất của lô) với con dấu đã ghi. */
    @Transactional(readOnly = true)
    public IntegrityReport verify(String traceabilityCode) {
        try {
            UUID.fromString(traceabilityCode);
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("Dữ liệu không hợp lệ",
                    List.of(new FieldErrorDetail("traceabilityCode", "Mã truy xuất không đúng định dạng")));
        }
        Long batchId = repository.findBatchIdByTraceabilityCode(traceabilityCode)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy lô hàng với mã truy xuất này"));

        Map<Long, LogRecord> logs = new LinkedHashMap<>();
        repository.findLogsByBatch(batchId).forEach(l -> logs.put(l.id(), l));
        List<SealRecord> seals = repository.findSealsByBatch(batchId);

        List<IntegrityIssue> issues = new ArrayList<>();
        Set<Long> sealedIds = new HashSet<>();
        for (SealRecord seal : seals) {
            sealedIds.add(seal.logId());
            LogRecord current = logs.get(seal.logId());
            if (current == null) {
                issues.add(new IntegrityIssue(seal.logId(), "DELETED"));
            } else if (!MessageDigest.isEqual(mac(current).getBytes(StandardCharsets.UTF_8),
                    seal.mac().getBytes(StandardCharsets.UTF_8))) {
                issues.add(new IntegrityIssue(seal.logId(), "MODIFIED"));
            }
        }
        int unsealed = (int) logs.keySet().stream().filter(id -> !sealedIds.contains(id)).count();
        return new IntegrityReport(traceabilityCode, batchId, issues.isEmpty() ? "OK" : "TAMPERED",
                sealedIds.size(), unsealed, issues);
    }

    /** HMAC-SHA256 của nội dung dòng nhật ký + mã truy xuất; mỗi trường có tiền tố độ dài để không bị nhập nhằng. */
    String mac(LogRecord log) {
        String canonical = field(log.id()) + field(log.batchId()) + field(log.traceabilityCode())
                + field(log.logDate()) + field(log.activity()) + field(log.note()) + field(log.imageUrl());
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(secret, "HmacSHA256"));
            String encoded = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(hmac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
            return encoded.substring(0, MAC_LENGTH);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Không tạo được chữ ký toàn vẹn", e);
        }
    }

    private static String field(Object value) {
        if (value == null) {
            return "~|";
        }
        String text = String.valueOf(value);
        return text.length() + ":" + text + "|";
    }
}
