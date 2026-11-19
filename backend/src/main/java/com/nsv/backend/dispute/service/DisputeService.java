package com.nsv.backend.dispute.service;

import com.nsv.backend.dispute.dto.*;

import java.util.List;

public interface DisputeService {
    DisputeDetailResponse createComplaint(Long buyerId, Long orderId, CreateComplaintRequest request);
    DisputeDetailResponse submitFarmerResponse(Long farmerId, Long complaintId, FarmerDisputeResponseRequest request);
    List<DisputeDetailResponse> getAllDisputesForAdmin(String status);
    DisputeDetailResponse getDisputeDetailForAdmin(Long complaintId);
    DisputeDetailResponse resolveDisputeByAdmin(Long adminId, Long complaintId, ResolveDisputeRequest request);
}
