package com.nsv.backend.complaint.controller;

import com.nsv.backend.common.security.AuthenticatedUser;
import com.nsv.backend.complaint.dto.ComplaintResponseRequest;
import com.nsv.backend.complaint.dto.ComplaintSlaResponse;
import com.nsv.backend.complaint.dto.FarmerComplaintItem;
import com.nsv.backend.complaint.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** PBI-30: Farmer xem và phản hồi khiếu nại (đường dẫn /api/v1/farmer/** chỉ dành cho FARMER). */
@RestController
@RequestMapping("/api/v1/farmer/complaints")
@RequiredArgsConstructor
public class FarmerComplaintController {

    private final ComplaintService complaintService;

    @GetMapping
    public List<FarmerComplaintItem> list(@AuthenticationPrincipal AuthenticatedUser user) {
        return complaintService.listForFarmer(user.id());
    }

    @PutMapping("/{id}/response")
    public ComplaintSlaResponse respond(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id,
            @Valid @RequestBody ComplaintResponseRequest request) {
        return complaintService.respond(user.id(), id, request.response());
    }
}
