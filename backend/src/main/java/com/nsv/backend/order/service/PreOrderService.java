package com.nsv.backend.order.service;

import com.nsv.backend.order.dto.CreatePreOrderRequest;
import com.nsv.backend.order.dto.PayDepositRequest;
import com.nsv.backend.order.dto.PreOrderResponse;

import java.util.List;

public interface PreOrderService {
    PreOrderResponse createPreOrder(Long buyerId, CreatePreOrderRequest request);
    PreOrderResponse payPreOrderDeposit(Long buyerId, Long orderId, PayDepositRequest request);
    List<PreOrderResponse> getBuyerPreOrders(Long buyerId);
    PreOrderResponse getBuyerPreOrderDetail(Long buyerId, Long orderId);
}
