package com.nsv.backend.inventory.controller;

import com.nsv.backend.common.security.AuthenticatedUser;
import com.nsv.backend.inventory.dto.BatchCreateRequest;
import com.nsv.backend.inventory.dto.BatchResponse;
import com.nsv.backend.inventory.dto.BatchUpdateRequest;
import com.nsv.backend.inventory.service.BatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Chỉ Farmer dùng được: SecurityConfig chặn /api/v1/farmer/** cho vai trò khác. */
@RestController
@RequestMapping("/api/v1/farmer/batches")
@RequiredArgsConstructor
public class FarmerBatchController {

    private final BatchService batchService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BatchResponse create(@AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody BatchCreateRequest request) {
        return batchService.create(principal.id(), request);
    }

    /** upcomingOnly=true: chỉ lấy các lô có ngày thu hoạch từ hôm nay trở đi. */
    @GetMapping
    public List<BatchResponse> list(@AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam(defaultValue = "false") boolean upcomingOnly) {
        return batchService.list(principal.id(), upcomingOnly);
    }

    @GetMapping("/{id}")
    public BatchResponse get(@AuthenticationPrincipal AuthenticatedUser principal, @PathVariable Long id) {
        return batchService.get(principal.id(), id);
    }

    @PutMapping("/{id}")
    public BatchResponse update(@AuthenticationPrincipal AuthenticatedUser principal, @PathVariable Long id,
            @Valid @RequestBody BatchUpdateRequest request) {
        return batchService.update(principal.id(), id, request);
    }
}
