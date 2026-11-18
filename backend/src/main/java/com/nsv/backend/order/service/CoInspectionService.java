package com.nsv.backend.order.service;

import com.nsv.backend.order.dto.CoInspectionResponse;
import com.nsv.backend.order.dto.CreateCoInspectionRequest;

import java.util.List;

public interface CoInspectionService {
    CoInspectionResponse requestCoInspection(Long buyerId, Long orderId, CreateCoInspectionRequest request);
    CoInspectionResponse getOrderCoInspection(Long actorId, String role, Long orderId);
    List<CoInspectionResponse> getFarmerCoInspections(Long farmerId);
    CoInspectionResponse acknowledgeCoInspection(Long farmerId, Long orderId);
}
