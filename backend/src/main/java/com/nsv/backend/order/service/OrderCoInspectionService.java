package com.nsv.backend.order.service;

import com.nsv.backend.order.dto.CoInspectionResponse;
import com.nsv.backend.order.dto.ConfirmOrderDeliveryRequest;
import com.nsv.backend.order.dto.RecordCoInspectionRequest;

public interface OrderCoInspectionService {
    CoInspectionResponse recordCoInspection(
        Long buyerId,
        Long orderId,
        RecordCoInspectionRequest request
    );

    CoInspectionResponse confirmDeliveryAndComplete(
        Long buyerId,
        Long orderId,
        ConfirmOrderDeliveryRequest request
    );

    CoInspectionResponse getCoInspectionByOrder(
        Long actorId,
        String role,
        Long orderId
    );
}
