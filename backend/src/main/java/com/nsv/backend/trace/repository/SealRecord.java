package com.nsv.backend.trace.repository;

/** Con dấu (chữ ký HMAC) của một dòng nhật ký, ghi lúc niêm phong. */
public record SealRecord(Long logId, Long batchId, String mac) {
}
