package com.nsv.backend.admin.scheduler;

import com.nsv.backend.admin.dto.BackupInfo;
import com.nsv.backend.admin.service.BackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Sao lưu tự động theo lịch (mặc định 02:00 mỗi ngày, múi giờ Asia/Ho_Chi_Minh).
 * Đổi bằng app.backup.cron / app.backup.zone; tắt bằng app.backup.enabled=false.
 */
@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.backup.enabled", havingValue = "true", matchIfMissing = true)
public class BackupScheduler {

    private final BackupService backupService;

    @Scheduled(cron = "${app.backup.cron:0 0 2 * * *}", zone = "${app.backup.zone:Asia/Ho_Chi_Minh}")
    public void runScheduledBackup() {
        try {
            BackupInfo info = backupService.createBackup();
            log.info("Đã sao lưu dữ liệu giao dịch: {} ({})", info.name(), info.rows());
        } catch (RuntimeException e) {
            log.error("Sao lưu theo lịch thất bại: {}", e.getMessage(), e);
        }
    }
}
