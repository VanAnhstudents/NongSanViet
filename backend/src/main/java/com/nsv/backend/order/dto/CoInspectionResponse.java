package com.nsv.backend.order.dto;

import com.nsv.backend.order.entity.OrderCoInspection;

import java.time.OffsetDateTime;

public record CoInspectionResponse(
    Long id,
    Long orderId,
    Long buyerId,
    String result,
    String resultLabel,
    boolean passed,
    String notes,
    boolean locked,
    String orderStatus,
    OffsetDateTime confirmedAt,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    public static CoInspectionResponse from(OrderCoInspection entity, String orderStatus) {
        boolean isPassed = entity.isPassed();
        return new CoInspectionResponse(
            entity.getId(),
            entity.getOrderId(),
            entity.getBuyerId(),
            entity.getResult(),
            isPassed ? "Đạt yêu cầu đồng kiểm" : "Không đạt yêu cầu đồng kiểm",
            isPassed,
            entity.getNotes(),
            entity.isLocked(),
            orderStatus,
            entity.getConfirmedAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
