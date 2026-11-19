package com.nsv.backend.trace.scheduler;

import com.nsv.backend.trace.service.FarmingLogIntegrityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Niêm phong nhật ký định kỳ (mặc định 5 phút/lần). Một dòng nhật ký được coi là "đã công khai" kể từ lần niêm phong
 * đầu tiên; trước đó Farmer còn một khoảng ngắn để sửa lỗi nhập liệu. Đổi bằng app.integrity.seal-interval-ms,
 * tắt bằng app.integrity.enabled=false.
 */
@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.integrity.enabled", havingValue = "true", matchIfMissing = true)
public class FarmingLogSealScheduler {

    private final FarmingLogIntegrityService integrityService;

    @Scheduled(fixedDelayString = "${app.integrity.seal-interval-ms:300000}")
    public void sealNewLogs() {
        try {
            int sealed = integrityService.sealUnsealedLogs();
            if (sealed > 0) {
                log.info("Đã niêm phong {} dòng nhật ký canh tác", sealed);
            }
        } catch (RuntimeException e) {
            log.error("Niêm phong nhật ký thất bại: {}", e.getMessage(), e);
        }
    }
}
