package com.nsv.backend.complaint.scheduler;

import com.nsv.backend.complaint.service.ComplaintService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/** Định kỳ (mặc định 5 phút) leo thang các khiếu nại quá hạn SLA. */
@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class ComplaintSlaScheduler {

    private final ComplaintService complaintService;

    @Scheduled(fixedDelayString = "${app.complaint.sla-check-ms:300000}")
    public void escalateOverdueComplaints() {
        int escalated = complaintService.escalateOverdue();
        if (escalated > 0) {
            log.info("Đã chuyển cấp {} khiếu nại quá hạn SLA lên Admin", escalated);
        }
    }
}
