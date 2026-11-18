package com.nsv.backend.admin.service;

import com.nsv.backend.admin.backup.BackupException;
import com.nsv.backend.admin.backup.TransactionDataStore;
import com.nsv.backend.admin.dto.BackupInfo;
import com.nsv.backend.common.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * PBI-39: sao lưu dữ liệu giao dịch định kỳ và khôi phục từ bản gần nhất.
 * Mỗi bản sao lưu là một thư mục backup-yyyyMMdd-HHmmss gồm các file CSV và manifest.properties
 * (manifest được ghi sau cùng nên chỉ bản đầy đủ mới được tính).
 */
@Service
public class BackupService {

    static final String PREFIX = "backup-";
    static final String MANIFEST = "manifest.properties";
    private static final DateTimeFormatter NAME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final TransactionDataStore store;
    private final Clock clock;
    private final Path baseDir;
    private final int retention;

    public BackupService(TransactionDataStore store, Clock clock,
            @Value("${app.backup.dir:./backups}") String dir,
            @Value("${app.backup.retention:7}") int retention) {
        this.store = store;
        this.clock = clock;
        this.baseDir = Path.of(dir);
        this.retention = Math.max(retention, 1);
    }

    public synchronized BackupInfo createBackup() {
        ZonedDateTime now = ZonedDateTime.now(clock);
        String name = PREFIX + NAME_FORMAT.format(now);
        Path dir = baseDir.resolve(name);
        try {
            Files.createDirectories(baseDir);
            Files.createDirectory(dir);
        } catch (IOException e) {
            throw failed("Không tạo được thư mục sao lưu " + name + ": " + e.getMessage());
        }
        try {
            Map<String, Long> rows = store.exportAll(dir);
            writeManifest(dir, now.toInstant(), rows);
            pruneOldBackups();
            return new BackupInfo(name, now.toInstant(), rows);
        } catch (BackupException | IOException e) {
            deleteQuietly(dir);
            throw failed("Sao lưu thất bại: " + e.getMessage());
        }
    }

    /** Các bản sao lưu đầy đủ, mới nhất trước. */
    public synchronized List<BackupInfo> list() {
        if (!Files.isDirectory(baseDir)) {
            return List.of();
        }
        try (Stream<Path> children = Files.list(baseDir)) {
            return children
                    .filter(Files::isDirectory)
                    .filter(p -> p.getFileName().toString().startsWith(PREFIX))
                    .filter(p -> Files.isRegularFile(p.resolve(MANIFEST)))
                    .sorted(Comparator.comparing((Path p) -> p.getFileName().toString()).reversed())
                    .map(this::readInfo)
                    .toList();
        } catch (IOException e) {
            throw failed("Không đọc được danh sách bản sao lưu: " + e.getMessage());
        }
    }

    /** Khôi phục dữ liệu giao dịch từ bản sao lưu gần nhất. */
    public synchronized BackupInfo restoreLatest() {
        List<BackupInfo> backups = list();
        if (backups.isEmpty()) {
            throw ApiException.notFound("Chưa có bản sao lưu nào để khôi phục");
        }
        BackupInfo latest = backups.get(0);
        Path dir = baseDir.resolve(latest.name());
        for (String table : TransactionDataStore.TABLES) {
            if (!Files.isRegularFile(dir.resolve(table + ".csv"))) {
                throw new ApiException(HttpStatus.CONFLICT, "INVALID_STATE",
                        "Bản sao lưu " + latest.name() + " không đầy đủ: thiếu " + table + ".csv", null);
            }
        }
        try {
            store.restoreAll(dir);
        } catch (BackupException e) {
            throw failed("Khôi phục thất bại, dữ liệu hiện tại được giữ nguyên: " + e.getMessage());
        }
        return latest;
    }

    private void writeManifest(Path dir, Instant createdAt, Map<String, Long> rows) throws IOException {
        Properties properties = new Properties();
        properties.setProperty("createdAt", createdAt.toString());
        rows.forEach((table, count) -> properties.setProperty("rows." + table, String.valueOf(count)));
        try (Writer writer = Files.newBufferedWriter(dir.resolve(MANIFEST), StandardCharsets.UTF_8)) {
            properties.store(writer, "Sao luu du lieu giao dich");
        }
    }

    private BackupInfo readInfo(Path dir) {
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(dir.resolve(MANIFEST), StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException e) {
            throw failed("Không đọc được manifest của " + dir.getFileName() + ": " + e.getMessage());
        }
        Map<String, Long> rows = new LinkedHashMap<>();
        for (String table : TransactionDataStore.TABLES) {
            String value = properties.getProperty("rows." + table);
            if (value != null) {
                rows.put(table, Long.parseLong(value));
            }
        }
        return new BackupInfo(dir.getFileName().toString(), Instant.parse(properties.getProperty("createdAt")), rows);
    }

    private void pruneOldBackups() {
        List<BackupInfo> backups = list();
        for (int i = retention; i < backups.size(); i++) {
            deleteQuietly(baseDir.resolve(backups.get(i).name()));
        }
    }

    private static void deleteQuietly(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
        } catch (IOException ignored) {
            // dọn dẹp cố gắng hết sức, không làm hỏng luồng chính
        }
    }

    private static ApiException failed(String message) {
        return new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "BACKUP_FAILED", message, null);
    }
}
