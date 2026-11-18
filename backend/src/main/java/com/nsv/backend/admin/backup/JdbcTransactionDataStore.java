package com.nsv.backend.admin.backup;

import lombok.RequiredArgsConstructor;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

/** Cài đặt cho PostgreSQL bằng lệnh COPY của pgjdbc (không cần cài pg_dump/psql trên máy chạy ứng dụng). */
@Component
@RequiredArgsConstructor
public class JdbcTransactionDataStore implements TransactionDataStore {

    private final DataSource dataSource;

    @Override
    public Map<String, Long> exportAll(Path directory) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            connection.setReadOnly(true);
            connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            CopyManager copy = connection.unwrap(PGConnection.class).getCopyAPI();

            Map<String, Long> rows = new LinkedHashMap<>();
            for (String table : TABLES) {
                try (Writer writer = Files.newBufferedWriter(directory.resolve(table + ".csv"),
                        StandardCharsets.UTF_8)) {
                    rows.put(table, copy.copyOut("COPY (SELECT * FROM " + table + " ORDER BY id) "
                            + "TO STDOUT WITH (FORMAT csv, HEADER true)", writer));
                }
            }
            connection.commit();
            return rows;
        } catch (SQLException | IOException e) {
            throw new BackupException("Không xuất được dữ liệu: " + e.getMessage(), e);
        }
    }

    @Override
    public void restoreAll(Path directory) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                CopyManager copy = connection.unwrap(PGConnection.class).getCopyAPI();
                try (Statement statement = connection.createStatement()) {
                    statement.execute("TRUNCATE TABLE " + String.join(", ", TABLES));
                }
                for (String table : TABLES) {
                    Path file = directory.resolve(table + ".csv");
                    String columns = readColumns(file);
                    try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                        copy.copyIn("COPY " + table + " (" + columns + ") FROM STDIN "
                                + "WITH (FORMAT csv, HEADER true)", reader);
                    }
                }
                try (Statement statement = connection.createStatement()) {
                    for (String table : TABLES) {
                        statement.execute("SELECT setval(pg_get_serial_sequence('" + table + "', 'id'), "
                                + "COALESCE((SELECT MAX(id) FROM " + table + "), 1), "
                                + "(SELECT MAX(id) FROM " + table + ") IS NOT NULL)");
                    }
                }
                connection.commit();
            } catch (SQLException | IOException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException | IOException e) {
            throw new BackupException("Không khôi phục được dữ liệu: " + e.getMessage(), e);
        }
    }

    /** Dòng đầu của file CSV là tên cột; dùng nó để nạp đúng cột kể cả khi thứ tự cột trong bảng thay đổi. */
    private static String readColumns(Path file) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null || header.isBlank()) {
                throw new IOException("File " + file.getFileName() + " rỗng");
            }
            return header;
        }
    }
}
