package com.nsv.backend.admin.dto;

import java.time.Instant;
import java.util.Map;

/** Thông tin một bản sao lưu: tên thư mục, thời điểm tạo và số dòng của từng bảng. */
public record BackupInfo(String name, Instant createdAt, Map<String, Long> rows) {
}
