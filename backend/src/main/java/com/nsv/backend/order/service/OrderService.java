package com.nsv.backend.order.service;

import com.nsv.backend.order.dto.CheckoutRequest;
import com.nsv.backend.order.dto.OrderResponse;

public interface OrderService {
    OrderResponse checkoutFromCart(Long buyerId, CheckoutRequest request);
    OrderResponse getOrderDetail(Long buyerId, Long orderId);
}
