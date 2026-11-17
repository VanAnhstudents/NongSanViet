package com.nsv.backend.order.service;

import com.nsv.backend.order.dto.AddToCartRequest;
import com.nsv.backend.order.dto.CartResponse;
import com.nsv.backend.order.dto.UpdateCartItemRequest;

public interface CartService {
    CartResponse getCart(Long buyerId);
    CartResponse addItem(Long buyerId, AddToCartRequest request);
    CartResponse updateItemQuantity(Long buyerId, Long cartItemId, UpdateCartItemRequest request);
    CartResponse removeItem(Long buyerId, Long cartItemId);
}
