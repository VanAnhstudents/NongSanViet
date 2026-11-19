package com.nsv.backend.trace;

import com.nsv.backend.common.exception.ApiException;
import com.nsv.backend.trace.dto.IntegrityReport;
import com.nsv.backend.trace.repository.FarmingLogIntegrityRepository;
import com.nsv.backend.trace.repository.LogRecord;
import com.nsv.backend.trace.repository.SealRecord;
import com.nsv.backend.trace.service.FarmingLogIntegrityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TcIntegrity01Test {

    private static final String CODE = "3f2b8c1e-6a4d-4e0b-9a57-1d2c3b4a5f60";
    private static final long BATCH_ID = 1L;

    /** Kho giả trong bộ nhớ: nhật ký và con dấu. */
    static class FakeRepository implements FarmingLogIntegrityRepository {
        final List<LogRecord> logs = new ArrayList<>();
        final List<SealRecord> seals = new ArrayList<>();

        @Override
        public Optional<Long> findBatchIdByTraceabilityCode(String traceabilityCode) {
            return CODE.equals(traceabilityCode) ? Optional.of(BATCH_ID) : Optional.empty();
        }

        @Override
        public List<LogRecord> findLogsByBatch(Long batchId) {
            return logs.stream().filter(l -> l.batchId().equals(batchId)).toList();
        }

        @Override
        public List<LogRecord> findUnsealedLogs() {
            return logs.stream().filter(l -> !isSealed(l.id())).toList();
        }

        @Override
        public List<SealRecord> findSealsByBatch(Long batchId) {
            return seals.stream().filter(s -> s.batchId().equals(batchId)).toList();
        }

        @Override
        public boolean isSealed(Long logId) {
            return seals.stream().anyMatch(s -> s.logId().equals(logId));
        }

        @Override
        public void saveSeal(Long logId, Long batchId, String mac) {
            seals.add(new SealRecord(logId, batchId, mac));
        }
    }

    private FakeRepository repository;
    private FarmingLogIntegrityService service;

    private static LogRecord log(long id, String activity, String note) {
        return new LogRecord(id, BATCH_ID, CODE, LocalDate.of(2026, 11, 10), activity, note, null);
    }

    @BeforeEach
    void setUp() {
        repository = new FakeRepository();
        service = new FarmingLogIntegrityService(repository, "test-integrity-secret");
        repository.logs.add(log(1, "Bón phân", "Phân hữu cơ"));
        repository.logs.add(log(2, "Tưới nước", null));
    }

    @Test
    @DisplayName("TC-INTEGRITY-01: Nhật ký đã niêm phong (công khai) không thể sửa/xóa -> bị từ chối")
    void tc01_sealedLogCannotBeEditedOrDeleted() {
        service.sealUnsealedLogs();

        assertThatThrownBy(() -> service.assertEditable(1L)).isInstanceOf(ApiException.class);
    }

    @Test
    @DisplayName("TC-INTEGRITY-01: Nhật ký mới bổ sung (chưa niêm phong) thì không bị chặn")
    void tc01_newLogIsNotBlocked() {
        service.sealUnsealedLogs();
        repository.logs.add(log(3, "Thu hoạch", "5 tạ"));

        assertThatCode(() -> service.assertEditable(3L)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("TC-INTEGRITY-01: Dữ liệu gắn với QR khớp 100% với dữ liệu gốc sau khi niêm phong")
    void tc01_verifyMatchesOriginal() {
        assertThat(service.sealUnsealedLogs()).isEqualTo(2);

        IntegrityReport report = service.verify(CODE);

        assertThat(report.status()).isEqualTo("OK");
        assertThat(report.sealedLogs()).isEqualTo(2);
        assertThat(report.unsealedLogs()).isZero();
        assertThat(report.issues()).isEmpty();
    }

    @Test
    @DisplayName("TC-INTEGRITY-01: Sửa trực tiếp nội dung nhật ký đã niêm phong -> bị phát hiện (MODIFIED)")
    void tc01_modifiedLogIsDetected() {
        service.sealUnsealedLogs();
        repository.logs.set(0, log(1, "Bón phân", "Phân hóa học (sửa trái phép)"));

        IntegrityReport report = service.verify(CODE);

        assertThat(report.status()).isEqualTo("TAMPERED");
        assertThat(report.issues()).extracting(i -> i.logId() + ":" + i.type()).containsExactly("1:MODIFIED");
    }

    @Test
    @DisplayName("TC-INTEGRITY-01: Xóa trực tiếp nhật ký đã niêm phong -> bị phát hiện (DELETED)")
    void tc01_deletedLogIsDetected() {
        service.sealUnsealedLogs();
        repository.logs.remove(1);

        IntegrityReport report = service.verify(CODE);

        assertThat(report.status()).isEqualTo("TAMPERED");
        assertThat(report.issues()).extracting(i -> i.logId() + ":" + i.type()).containsExactly("2:DELETED");
    }

    @Test
    @DisplayName("TC-INTEGRITY-01: Chỉ bổ sung mục mới thì vẫn khớp 100% (mục mới tính là chưa niêm phong)")
    void tc01_appendingNewLogKeepsIntegrity() {
        service.sealUnsealedLogs();
        repository.logs.add(log(3, "Thu hoạch", "5 tạ"));

        IntegrityReport report = service.verify(CODE);

        assertThat(report.status()).isEqualTo("OK");
        assertThat(report.sealedLogs()).isEqualTo(2);
        assertThat(report.unsealedLogs()).isEqualTo(1);
    }
}
