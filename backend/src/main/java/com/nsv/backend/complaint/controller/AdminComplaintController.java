package com.nsv.backend.complaint.controller;

import com.nsv.backend.complaint.dto.EscalatedComplaintItem;
import com.nsv.backend.complaint.service.ComplaintService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** PBI-30: Admin xem các khiếu nại đã bị chuyển cấp do Farmer không phản hồi trong 24h. */
@RestController
@RequestMapping("/api/v1/admin/complaints")
@RequiredArgsConstructor
public class AdminComplaintController {

    private final ComplaintService complaintService;

    @GetMapping("/escalated")
    public List<EscalatedComplaintItem> escalated() {
        return complaintService.listEscalated();
    }
}
