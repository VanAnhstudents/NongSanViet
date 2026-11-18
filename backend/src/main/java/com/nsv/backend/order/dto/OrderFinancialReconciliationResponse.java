package com.nsv.backend.order.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderFinancialReconciliationResponse(
    Long orderId,
    String orderStatus,
    String paymentMethod,
    BigDecimal orderTotal,
    BigDecimal totalPaid,
    BigDecimal totalRefunded,
    BigDecimal netCollected,
    boolean exactAmountMatch,
    List<PaymentResponse> payments,
    List<RefundResponse> refunds
) {}
