package com.nsv.backend.common.exception;

import java.time.Instant;
import java.util.List;

/** Định dạng lỗi thống nhất cho toàn bộ API. */
public record ApiError(String timestamp, int status, String code, String message,
                       List<FieldErrorDetail> fieldErrors) {

    public static ApiError of(int status, String code, String message, List<FieldErrorDetail> fieldErrors) {
        return new ApiError(Instant.now().toString(), status, code, message,
                fieldErrors == null ? List.of() : fieldErrors);
    }
}
