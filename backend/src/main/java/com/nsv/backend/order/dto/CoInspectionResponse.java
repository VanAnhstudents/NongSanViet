package com.nsv.backend.order.dto;

import com.nsv.backend.order.entity.OrderCoInspection;

import java.time.OffsetDateTime;

public record CoInspectionResponse(
    Long id,
    Long orderId,
    String orderStatus,
    Long buyerId,
    boolean checkQuantity,
    boolean checkQuality,
    String note,
    String status,
    String deliveryInstruction,
    Long acknowledgedBy,
    OffsetDateTime acknowledgedAt,
    OffsetDateTime createdAt
) {
    public static CoInspectionResponse from(OrderCoInspection entity, String orderStatus) {
        StringBuilder instruction = new StringBuilder("Yêu cầu đồng kiểm khi giao hàng: ");
        if (entity.isCheckQuantity() && entity.isCheckQuality()) {
            instruction.append("Kiểm đếm đủ số lượng/trọng lượng và kiểm tra chất lượng nông sản");
        } else if (entity.isCheckQuantity()) {
            instruction.append("Kiểm đếm đủ số lượng/trọng lượng");
        } else if (entity.isCheckQuality()) {
            instruction.append("Kiểm tra chất lượng/ngoại quan nông sản");
        }
        if (entity.getNote() != null && !entity.getNote().isBlank()) {
            instruction.append(" (Ghi chú: ").append(entity.getNote()).append(")");
        }

        return new CoInspectionResponse(
            entity.getId(),
            entity.getOrderId(),
            orderStatus,
            entity.getBuyerId(),
            entity.isCheckQuantity(),
            entity.isCheckQuality(),
            entity.getNote(),
            entity.getStatus(),
            instruction.toString(),
            entity.getAcknowledgedBy(),
            entity.getAcknowledgedAt(),
            entity.getCreatedAt()
        );
    }
}
