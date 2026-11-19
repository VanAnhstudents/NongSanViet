package com.nsv.backend.order.api;

import com.nsv.backend.order.api.dto.OrderDisputeContextDto;

import java.math.BigDecimal;

public interface OrderModuleApi {
    OrderDisputeContextDto getOrderDisputeContext(Long orderId);
    boolean isOrderOwnedByFarmer(Long orderId, Long farmerId);
    BigDecimal executeDisputeRefund(Long adminId, Long orderId, Long complaintId, BigDecimal amount, String reason);
    void updateOrderStatusAfterDispute(Long orderId, String newStatus);
}
