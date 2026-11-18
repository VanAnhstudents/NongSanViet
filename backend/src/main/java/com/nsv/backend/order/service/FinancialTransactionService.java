package com.nsv.backend.order.service;

import com.nsv.backend.order.dto.*;

import java.util.List;

public interface FinancialTransactionService {
    PaymentResponse processPayment(Long actorId, boolean isAdmin, ProcessPaymentRequest request);
    RefundResponse processRefund(Long adminId, ProcessRefundRequest request);
    List<PaymentResponse> getOrderPayments(Long actorId, String role, Long orderId);
    OrderFinancialReconciliationResponse reconcileOrderFinances(Long orderId);
}
