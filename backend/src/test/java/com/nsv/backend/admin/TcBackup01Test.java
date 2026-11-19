package com.nsv.backend.admin;

import com.nsv.backend.admin.backup.TransactionDataStore;
import com.nsv.backend.admin.dto.BackupInfo;
import com.nsv.backend.admin.scheduler.BackupScheduler;
import com.nsv.backend.admin.service.BackupService;
import com.nsv.backend.common.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TcBackup01Test {

    /** Đồng hồ chỉnh được, bắt đầu 02:00 UTC ngày 2026-11-17. */
    static class TestClock extends Clock {
        private Instant now = Instant.parse("2026-11-17T02:00:00Z");

        void advanceHours(long hours) {
            now = now.plusSeconds(hours * 3600);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    /** Kho giả: ghi file CSV tối thiểu cho từng bảng và nhớ thư mục được khôi phục. */
    static class FakeStore implements TransactionDataStore {
        Path restoredFrom;

        @Override
        public Map<String, Long> exportAll(Path directory) {
            Map<String, Long> rows = new LinkedHashMap<>();
            for (String table : TABLES) {
                try {
                    Files.writeString(directory.resolve(table + ".csv"), "id\n1\n2\n3\n");
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                rows.put(table, 3L);
            }
            return rows;
        }

        @Override
        public void restoreAll(Path directory) {
            restoredFrom = directory;
        }
    }

    @TempDir
    Path tempDir;

    private TestClock clock;
    private FakeStore store;
    private BackupService service;

    @BeforeEach
    void setUp() {
        clock = new TestClock();
        store = new FakeStore();
        service = new BackupService(store, clock, tempDir.toString(), 7);
    }

    @Test
    @DisplayName("TC-BACKUP-01: Sao lưu tạo bản đầy đủ: mọi bảng giao dịch và manifest, ghi nhận số dòng")
    void tc01_backupIsComplete() {
        BackupInfo info = service.createBackup();

        assertThat(info.name()).isEqualTo("backup-20261117-020000");
        assertThat(info.rows()).hasSize(TransactionDataStore.TABLES.size()).containsEntry("orders", 3L);
        Path dir = tempDir.resolve(info.name());
        for (String table : TransactionDataStore.TABLES) {
            assertThat(dir.resolve(table + ".csv")).isRegularFile();
        }
        assertThat(dir.resolve("manifest.properties")).isRegularFile();
        assertThat(service.list()).extracting(BackupInfo::name).containsExactly("backup-20261117-020000");
    }

    @Test
    @DisplayName("TC-BACKUP-01: Lịch định kỳ tự động chạy sao lưu")
    void tc01_scheduledJobCreatesBackup() {
        new BackupScheduler(service).runScheduledBackup();

        assertThat(service.list()).hasSize(1);
    }

    @Test
    @DisplayName("TC-BACKUP-01: Khôi phục dùng đúng bản sao lưu gần nhất")
    void tc01_restoreUsesLatestBackup() {
        service.createBackup();
        clock.advanceHours(24);
        service.createBackup();

        BackupInfo restored = service.restoreLatest();

        assertThat(restored.name()).isEqualTo("backup-20261118-020000");
        assertThat(store.restoredFrom).isEqualTo(tempDir.resolve("backup-20261118-020000"));
    }

    @Test
    @DisplayName("TC-BACKUP-01: Chưa có bản sao lưu nào -> không khôi phục được")
    void tc01_restoreWithoutBackupFails() {
        assertThatThrownBy(() -> service.restoreLatest()).isInstanceOf(ApiException.class);

        assertThat(store.restoredFrom).isNull();
    }

    @Test
    @DisplayName("TC-BACKUP-01: Bản sao lưu thiếu file thì không khôi phục, dữ liệu hiện tại không bị đụng tới")
    void tc01_incompleteBackupIsNotRestored() throws IOException {
        BackupInfo info = service.createBackup();
        Files.delete(tempDir.resolve(info.name()).resolve("refunds.csv"));

        assertThatThrownBy(() -> service.restoreLatest()).isInstanceOf(ApiException.class);

        assertThat(store.restoredFrom).isNull();
    }
}
