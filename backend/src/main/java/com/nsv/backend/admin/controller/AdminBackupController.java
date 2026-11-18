package com.nsv.backend.admin.controller;

import com.nsv.backend.admin.dto.BackupInfo;
import com.nsv.backend.admin.dto.RestoreRequest;
import com.nsv.backend.admin.service.BackupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** PBI-39 (đường dẫn /api/v1/admin/** chỉ dành cho ADMIN). */
@RestController
@RequestMapping("/api/v1/admin/backups")
@RequiredArgsConstructor
public class AdminBackupController {

    private final BackupService backupService;

    /** Sao lưu ngay (ngoài lịch định kỳ). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BackupInfo backupNow() {
        return backupService.createBackup();
    }

    @GetMapping
    public List<BackupInfo> list() {
        return backupService.list();
    }

    /** Khôi phục từ bản gần nhất. Xóa dữ liệu giao dịch hiện tại, nên bắt buộc gửi confirm = RESTORE. */
    @PostMapping("/restore-latest")
    public BackupInfo restoreLatest(@Valid @RequestBody RestoreRequest request) {
        return backupService.restoreLatest();
    }
}
