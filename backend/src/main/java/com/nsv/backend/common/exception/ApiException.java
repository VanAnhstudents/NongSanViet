package com.nsv.backend.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;
    private final List<FieldErrorDetail> fieldErrors;

    public ApiException(HttpStatus status, String code, String message, List<FieldErrorDetail> fieldErrors) {
        super(message);
        this.status = status;
        this.code = code;
        this.fieldErrors = fieldErrors == null ? List.of() : fieldErrors;
    }

    public static ApiException badRequest(String message, List<FieldErrorDetail> fieldErrors) {
        return new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, fieldErrors);
    }

    public static ApiException unauthorized(String message) {
        return new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", message, null);
    }

    public static ApiException forbidden(String message) {
        return new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", message, null);
    }

    public static ApiException notFound(String message) {
        return new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", message, null);
    }

    public static ApiException conflict(String message, List<FieldErrorDetail> fieldErrors) {
        return new ApiException(HttpStatus.CONFLICT, "DUPLICATE", message, fieldErrors);
    }
}
