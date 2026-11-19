package com.nsv.backend.trace.controller;

import com.nsv.backend.trace.dto.IntegrityReport;
import com.nsv.backend.trace.dto.SealResult;
import com.nsv.backend.trace.service.FarmingLogIntegrityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** PBI-35 (đường dẫn /api/v1/admin/** chỉ dành cho ADMIN). */
@RestController
@RequestMapping("/api/v1/admin/integrity")
@RequiredArgsConstructor
public class AdminIntegrityController {

    private final FarmingLogIntegrityService integrityService;

    /** Đối chiếu dữ liệu nhật ký gắn với mã QR của lô. */
    @GetMapping("/trace/{traceabilityCode}")
    public IntegrityReport verify(@PathVariable String traceabilityCode) {
        return integrityService.verify(traceabilityCode);
    }

    /** Niêm phong ngay các dòng nhật ký mới (ngoài lịch định kỳ). */
    @PostMapping("/seal")
    public SealResult sealNow() {
        return new SealResult(integrityService.sealUnsealedLogs());
    }
}
