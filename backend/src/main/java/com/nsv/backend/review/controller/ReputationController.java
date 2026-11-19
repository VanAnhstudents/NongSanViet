package com.nsv.backend.review.controller;

import com.nsv.backend.common.security.AuthenticatedUser;
import com.nsv.backend.review.dto.FarmerReputationResponse;
import com.nsv.backend.review.service.ReputationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** PBI-38: Buyer xem uy tín của Farmer; Farmer xem uy tín của chính mình. */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReputationController {

    private final ReputationService reputationService;

    @GetMapping("/buyer/farmers/{farmerId}/reputation")
    public FarmerReputationResponse farmerReputation(@PathVariable Long farmerId) {
        return reputationService.getReputation(farmerId);
    }

    @GetMapping("/farmer/reputation")
    public FarmerReputationResponse myReputation(@AuthenticationPrincipal AuthenticatedUser user) {
        return reputationService.getReputation(user.id());
    }
}
