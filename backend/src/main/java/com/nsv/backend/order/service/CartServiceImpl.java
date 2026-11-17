package com.nsv.backend.order.service;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.inventory.entity.Batch;
import com.nsv.backend.inventory.repository.BatchRepository;
import com.nsv.backend.order.dto.AddToCartRequest;
import com.nsv.backend.order.dto.CartItemResponse;
import com.nsv.backend.order.dto.CartResponse;
import com.nsv.backend.order.dto.UpdateCartItemRequest;
import com.nsv.backend.order.entity.CartItem;
import com.nsv.backend.order.repository.CartItemRepository;
import com.nsv.backend.product.entity.Product;
import com.nsv.backend.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final BatchRepository batchRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(Long buyerId) {
        return buildCartResponse(buyerId);
    }

    @Override
    @Transactional
    public CartResponse addItem(Long buyerId, AddToCartRequest request) {
        Batch batch = batchRepository.findById(request.batchId())
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "BATCH_NOT_FOUND", "Không tìm thấy lô sản phẩm"));

        validateBatchPurchasable(batch);

        CartItem cartItem = cartItemRepository.findByUserIdAndBatchId(buyerId, batch.getId())
            .orElseGet(() -> CartItem.builder()
                .userId(buyerId)
                .batchId(batch.getId())
                .quantity(BigDecimal.ZERO)
                .build());

        BigDecimal newQuantity = cartItem.getQuantity().add(request.quantity());
        if (newQuantity.compareTo(batch.getRemainingQuantity()) > 0) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "INSUFFICIENT_STOCK",
                "Số lượng vượt quá tồn kho khả dụng (" + batch.getRemainingQuantity() + ")"
            );
        }

        cartItem.setQuantity(newQuantity);
        cartItemRepository.save(cartItem);

        return buildCartResponse(buyerId);
    }

    @Override
    @Transactional
    public CartResponse updateItemQuantity(Long buyerId, Long cartItemId, UpdateCartItemRequest request) {
        CartItem cartItem = cartItemRepository.findByIdAndUserId(cartItemId, buyerId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "CART_ITEM_NOT_FOUND", "Không tìm thấy sản phẩm trong giỏ hàng"));

        Batch batch = batchRepository.findById(cartItem.getBatchId())
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "BATCH_NOT_FOUND", "Không tìm thấy lô sản phẩm"));

        validateBatchPurchasable(batch);

        if (request.quantity().compareTo(batch.getRemainingQuantity()) > 0) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "INSUFFICIENT_STOCK",
                "Số lượng vượt quá tồn kho khả dụng (" + batch.getRemainingQuantity() + ")"
            );
        }

        cartItem.setQuantity(request.quantity());
        cartItemRepository.save(cartItem);

        return buildCartResponse(buyerId);
    }

    @Override
    @Transactional
    public CartResponse removeItem(Long buyerId, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findByIdAndUserId(cartItemId, buyerId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "CART_ITEM_NOT_FOUND", "Không tìm thấy sản phẩm trong giỏ hàng"));

        cartItemRepository.delete(cartItem);
        return buildCartResponse(buyerId);
    }

    private void validateBatchPurchasable(Batch batch) {
        if (!"AVAILABLE".equals(batch.getStatus()) && !"PRE_ORDER".equals(batch.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "BATCH_NOT_AVAILABLE", "Lô sản phẩm hiện không mở bán");
        }
    }

    private CartResponse buildCartResponse(Long buyerId) {
        List<CartItem> cartItems = cartItemRepository.findAllByUserIdOrderByIdAsc(buyerId);
        if (cartItems.isEmpty()) {
            return new CartResponse(List.of(), 0, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }

        Set<Long> batchIds = cartItems.stream().map(CartItem::getBatchId).collect(Collectors.toSet());
        Map<Long, Batch> batchMap = batchRepository.findAllByIdIn(batchIds).stream()
            .collect(Collectors.toMap(Batch::getId, Function.identity()));

        Set<Long> productIds = batchMap.values().stream().map(Batch::getProductId).collect(Collectors.toSet());
        Map<Long, Product> productMap = productRepository.findAllByIdIn(productIds).stream()
            .collect(Collectors.toMap(Product::getId, Function.identity()));

        BigDecimal totalAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        List<CartItemResponse> itemResponses = new java.util.ArrayList<>(cartItems.size());

        for (CartItem item : cartItems) {
            Batch batch = batchMap.get(item.getBatchId());
            if (batch == null) {
                continue;
            }
            Product product = productMap.get(batch.getProductId());
            BigDecimal subtotal = batch.getUnitPrice()
                .multiply(item.getQuantity())
                .setScale(2, RoundingMode.HALF_UP);

            totalAmount = totalAmount.add(subtotal);

            itemResponses.add(new CartItemResponse(
                item.getId(),
                batch.getId(),
                batch.getProductId(),
                product != null ? product.getName() : null,
                product != null ? product.getUnit() : null,
                batch.getUnitPrice(),
                item.getQuantity(),
                subtotal,
                batch.getRemainingQuantity()
            ));
        }

        return new CartResponse(itemResponses, itemResponses.size(), totalAmount);
    }
}
