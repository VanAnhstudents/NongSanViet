package com.nsv.backend.order.service;

import com.nsv.backend.order.dto.CheckoutRequest;
import com.nsv.backend.order.dto.OrderResponse;
import com.nsv.backend.order.dto.UpdateOrderStatusRequest;

import java.util.List;

public interface OrderService {
    OrderResponse checkoutFromCart(Long buyerId, CheckoutRequest request);
    OrderResponse getOrderDetail(Long buyerId, Long orderId);
    List<OrderResponse> getBuyerOrders(Long buyerId);

    List<OrderResponse> getFarmerOrders(Long farmerId, String status);
    OrderResponse getFarmerOrderDetail(Long farmerId, Long orderId);
    OrderResponse updateOrderStatusByFarmer(Long farmerId, Long orderId, UpdateOrderStatusRequest request);
}
