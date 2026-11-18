package com.nsv.backend.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RestoreRequest(
        @NotBlank(message = "Gõ RESTORE để xác nhận khôi phục")
        @Pattern(regexp = "^RESTORE$", message = "Gõ RESTORE để xác nhận khôi phục")
        String confirm) {
}
