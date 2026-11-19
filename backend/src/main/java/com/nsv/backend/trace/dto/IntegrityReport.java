package com.nsv.backend.trace.dto;

import java.util.List;

/**
 * Kết quả đối chiếu dữ liệu gắn với mã QR.
 * status = OK khi mọi dòng đã niêm phong còn nguyên vẹn (100% khớp); TAMPERED khi có dòng bị sửa/xóa.
 * unsealedLogs là các dòng mới bổ sung, chưa niêm phong (được phép).
 */
public record IntegrityReport(String traceabilityCode, Long batchId, String status,
        int sealedLogs, int unsealedLogs, List<IntegrityIssue> issues) {
}
