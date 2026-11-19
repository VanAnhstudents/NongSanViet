package com.nsv.backend.dispute.service;

import com.nsv.backend.dispute.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DisputeService {
    ComplaintEligibilityResponse checkComplaintEligibility(Long buyerId, Long orderId);

    DisputeDetailResponse createComplaint(Long buyerId, Long orderId, CreateComplaintRequest request);

    ComplaintSubmissionResponse createComplaintWithMedia(
        Long buyerId,
        Long orderId,
        String reason,
        String description,
        List<MultipartFile> files,
        List<String> captions
    );

    ComplaintSubmissionResponse getBuyerComplaintByOrder(Long buyerId, Long orderId);

    List<ComplaintSubmissionResponse> getBuyerComplaints(Long buyerId);

    DisputeDetailResponse submitFarmerResponse(Long farmerId, Long complaintId, FarmerDisputeResponseRequest request);

    List<DisputeDetailResponse> getAllDisputesForAdmin(String status);

    DisputeDetailResponse getDisputeDetailForAdmin(Long complaintId);

    DisputeDetailResponse resolveDisputeByAdmin(Long adminId, Long complaintId, ResolveDisputeRequest request);
}
