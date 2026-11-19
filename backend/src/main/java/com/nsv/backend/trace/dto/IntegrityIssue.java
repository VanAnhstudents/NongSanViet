package com.nsv.backend.trace.dto;

/** type: MODIFIED (nội dung đã bị sửa so với lúc niêm phong) hoặc DELETED (dòng nhật ký đã bị xóa/chuyển khỏi lô). */
public record IntegrityIssue(Long logId, String type) {
}
