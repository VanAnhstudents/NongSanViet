package com.nsv.backend.order.service;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.inventory.entity.Batch;
import com.nsv.backend.inventory.repository.BatchRepository;
import com.nsv.backend.order.dto.*;
import com.nsv.backend.order.entity.CartItem;
import com.nsv.backend.order.entity.Order;
import com.nsv.backend.order.entity.OrderAddress;
import com.nsv.backend.order.entity.OrderItem;
import com.nsv.backend.order.repository.CartItemRepository;
import com.nsv.backend.order.repository.OrderRepository;
import com.nsv.backend.product.entity.Product;
import com.nsv.backend.product.repository.ProductRepository;
import com.nsv.backend.user.entity.Address;
import com.nsv.backend.user.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final Map<String, String> FARMER_ALLOWED_TRANSITIONS = Map.of(
        "PENDING", "CONFIRMED",    // Chờ xác nhận -> Đang xử lý
        "CONFIRMED", "SHIPPING",   // Đang xử lý -> Đang giao
        "SHIPPING", "DELIVERED"    // Đang giao -> Đã giao
    );

    private static final Set<String> VALID_ORDER_STATUSES = Set.of(
        "PENDING", "CONFIRMED", "SHIPPING", "DELIVERED", "COMPLETED", "CANCELLED"
    );

    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final BatchRepository batchRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public OrderResponse checkoutFromCart(Long buyerId, CheckoutRequest request) {
        List<CartItem> targetItems = (request.cartItemIds() == null || request.cartItemIds().isEmpty())
            ? cartItemRepository.findAllByUserIdOrderByIdAsc(buyerId)
            : cartItemRepository.findAllByUserIdAndIdInOrderByIdAsc(buyerId, request.cartItemIds());

        if (targetItems.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "EMPTY_CART", "Giỏ hàng trống, không thể đặt hàng");
        }
        if (request.cartItemIds() != null && !request.cartItemIds().isEmpty()
                && targetItems.size() != request.cartItemIds().size()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_CART_ITEMS", "Một số sản phẩm trong giỏ hàng không hợp lệ");
        }

        Address address = addressRepository.findByIdAndUser(request.addressId(), buyerId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "ADDRESS_NOT_FOUND", "Không tìm thấy địa chỉ nhận hàng"));

        String provinceName = resolveProvinceName(address.getProvince().getId());

        List<Long> sortedBatchIds = targetItems.stream()
            .map(CartItem::getBatchId)
            .distinct()
            .sorted()
            .toList();

        Map<Long, Batch> lockedBatchMap = batchRepository.findAllByIdInOrderByIdAscForUpdate(sortedBatchIds).stream()
            .collect(Collectors.toMap(Batch::getId, Function.identity()));

        if (lockedBatchMap.size() != sortedBatchIds.size()) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "BATCH_NOT_FOUND", "Một số lô sản phẩm không còn tồn tại");
        }

        BigDecimal shippingFee = (request.shippingFee() != null ? request.shippingFee() : BigDecimal.ZERO)
            .setScale(2, RoundingMode.HALF_UP);

        Order order = Order.builder()
            .buyerId(buyerId)
            .addressId(address.getId())
            .paymentMethod(request.paymentMethod())
            .status("PENDING")
            .shippingFee(shippingFee)
            .build();

        OrderAddress snapshotAddress = OrderAddress.builder()
            .receiverName(address.getReceiverName())
            .receiverPhone(address.getReceiverPhone())
            .provinceName(provinceName)
            .addressLine(address.getAddressLine())
            .build();
        order.attachAddress(snapshotAddress);

        for (CartItem cartItem : targetItems) {
            Batch batch = lockedBatchMap.get(cartItem.getBatchId());

            if (!"AVAILABLE".equals(batch.getStatus()) && !"PRE_ORDER".equals(batch.getStatus())) {
                throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "BATCH_NOT_AVAILABLE",
                    "Lô hàng #" + batch.getId() + " hiện không mở bán"
                );
            }
            if (batch.getRemainingQuantity().compareTo(cartItem.getQuantity()) < 0) {
                throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "INSUFFICIENT_STOCK",
                    "Lô hàng #" + batch.getId() + " không đủ tồn kho (còn lại: " + batch.getRemainingQuantity() + ")"
                );
            }

            BigDecimal newRemaining = batch.getRemainingQuantity().subtract(cartItem.getQuantity());
            batch.setRemainingQuantity(newRemaining);
            if (newRemaining.compareTo(BigDecimal.ZERO) == 0) {
                batch.setStatus("SOLD_OUT");
            }

            OrderItem orderItem = OrderItem.builder()
                .batchId(batch.getId())
                .quantity(cartItem.getQuantity())
                .unitPrice(batch.getUnitPrice())
                .build();
            order.addItem(orderItem);
        }

        batchRepository.saveAll(lockedBatchMap.values());
        Order savedOrder = orderRepository.save(order);
        cartItemRepository.deleteAll(targetItems);

        return toOrderResponse(savedOrder, lockedBatchMap);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetail(Long buyerId, Long orderId) {
        Order order = orderRepository.findWithDetailsByIdAndBuyerId(orderId, buyerId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Không tìm thấy đơn hàng"));

        return mapSingleOrder(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getBuyerOrders(Long buyerId) {
        List<Order> orders = orderRepository.findAllByBuyerIdOrderByCreatedAtDesc(buyerId);
        return mapOrderList(orders);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getFarmerOrders(Long farmerId, String statusFilter) {
        String normalizedStatus = null;
        if (StringUtils.hasText(statusFilter)) {
            normalizedStatus = statusFilter.trim().toUpperCase();
            if (!VALID_ORDER_STATUSES.contains(normalizedStatus)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_STATUS_FILTER", "Trạng thái lọc không hợp lệ");
            }
        }

        List<Order> orders = orderRepository.findAllByFarmerIdAndOptionalStatus(farmerId, normalizedStatus);
        return mapOrderList(orders);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getFarmerOrderDetail(Long farmerId, Long orderId) {
        Order order = findAndVerifyFarmerOrder(farmerId, orderId);
        return mapSingleOrder(order);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatusByFarmer(Long farmerId, Long orderId, UpdateOrderStatusRequest request) {
        Order order = findAndVerifyFarmerOrder(farmerId, orderId);

        String currentStatus = order.getStatus();
        String targetStatus = request.status().trim().toUpperCase();

        String expectedNextStatus = FARMER_ALLOWED_TRANSITIONS.get(currentStatus);
        if (expectedNextStatus == null || !expectedNextStatus.equals(targetStatus)) {
            String hint = expectedNextStatus != null
                ? "Trạng thái hợp lệ tiếp theo phải là " + expectedNextStatus
                : "Đơn hàng ở trạng thái " + currentStatus + " không thể cập nhật thêm";

            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "INVALID_STATUS_TRANSITION",
                "Không thể chuyển trạng thái đơn hàng từ " + currentStatus + " sang " + targetStatus + ". " + hint
            );
        }

        order.setStatus(targetStatus);
        if ("DELIVERED".equals(targetStatus)) {
            order.setDeliveredAt(OffsetDateTime.now());
        }

        Order savedOrder = orderRepository.save(order);

        notifyBuyerOrderStatusChanged(savedOrder);
        recordAuditLog(farmerId, "UPDATE_ORDER_STATUS", "orders", savedOrder.getId());

        return mapSingleOrder(savedOrder);
    }

    private Order findAndVerifyFarmerOrder(Long farmerId, Long orderId) {
        Order order = orderRepository.findWithDetailsById(orderId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Không tìm thấy đơn hàng"));

        if (!orderRepository.existsOrderForFarmer(orderId, farmerId)) {
            throw new BusinessException(
                HttpStatus.FORBIDDEN,
                "ORDER_ACCESS_DENIED",
                "Bạn không có quyền thao tác trên đơn hàng này"
            );
        }
        return order;
    }

    private void notifyBuyerOrderStatusChanged(Order order) {
        String statusLabel = switch (order.getStatus()) {
            case "CONFIRMED" -> "Đang xử lý (Đã xác nhận)";
            case "SHIPPING" -> "Đang giao hàng";
            case "DELIVERED" -> "Đã giao hàng thành công";
            default -> order.getStatus();
        };
        String content = String.format("Đơn hàng #%d của bạn đã chuyển sang trạng thái: %s.", order.getId(), statusLabel);

        jdbcTemplate.update(
            "INSERT INTO notifications (user_id, content, is_read) VALUES (?, ?, FALSE)",
            order.getBuyerId(),
            content
        );
    }

    private void recordAuditLog(Long actorId, String action, String entityType, Long entityId) {
        jdbcTemplate.update(
            "INSERT INTO audit_logs (actor_id, action, entity_type, entity_id) VALUES (?, ?, ?, ?)",
            actorId,
            action,
            entityType,
            entityId
        );
    }

    private String resolveProvinceName(Long provinceId) {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT name FROM provinces WHERE id = ?",
                String.class,
                provinceId
            );
        } catch (EmptyResultDataAccessException ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "PROVINCE_NOT_FOUND", "Tỉnh/thành phố không tồn tại");
        }
    }

    private OrderResponse mapSingleOrder(Order order) {
        Set<Long> batchIds = order.getItems().stream()
            .map(OrderItem::getBatchId)
            .collect(Collectors.toSet());

        Map<Long, Batch> batchMap = batchIds.isEmpty()
            ? Map.of()
            : batchRepository.findAllByIdIn(batchIds).stream()
                .collect(Collectors.toMap(Batch::getId, Function.identity()));

        return toOrderResponse(order, batchMap);
    }

    private List<OrderResponse> mapOrderList(List<Order> orders) {
        if (orders.isEmpty()) {
            return List.of();
        }
        Set<Long> allBatchIds = orders.stream()
            .flatMap(o -> o.getItems().stream())
            .map(OrderItem::getBatchId)
            .collect(Collectors.toSet());

        Map<Long, Batch> batchMap = allBatchIds.isEmpty()
            ? Map.of()
            : batchRepository.findAllByIdIn(allBatchIds).stream()
                .collect(Collectors.toMap(Batch::getId, Function.identity()));

        return orders.stream()
            .map(order -> toOrderResponse(order, batchMap))
            .toList();
    }

    private OrderResponse toOrderResponse(Order order, Map<Long, Batch> batchMap) {
        Set<Long> productIds = batchMap.values().stream()
            .map(Batch::getProductId)
            .collect(Collectors.toSet());

        Map<Long, Product> productMap = productIds.isEmpty()
            ? Map.of()
            : productRepository.findAllByIdIn(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        BigDecimal itemsSubtotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        List<OrderItemResponse> itemResponses = new ArrayList<>(order.getItems().size());

        for (OrderItem item : order.getItems()) {
            Batch batch = batchMap.get(item.getBatchId());
            Product product = (batch != null) ? productMap.get(batch.getProductId()) : null;

            BigDecimal subtotal = item.getUnitPrice()
                .multiply(item.getQuantity())
                .setScale(2, RoundingMode.HALF_UP);
            itemsSubtotal = itemsSubtotal.add(subtotal);

            itemResponses.add(new OrderItemResponse(
                item.getId(),
                item.getBatchId(),
                batch != null ? batch.getProductId() : null,
                product != null ? product.getName() : null,
                product != null ? product.getUnit() : null,
                item.getQuantity(),
                item.getUnitPrice(),
                subtotal
            ));
        }

        OrderAddress addr = order.getOrderAddress();
        OrderAddressResponse addressResponse = addr != null
            ? new OrderAddressResponse(
                addr.getReceiverName(),
                addr.getReceiverPhone(),
                addr.getProvinceName(),
                addr.getAddressLine()
            )
            : null;

        BigDecimal totalAmount = itemsSubtotal.add(order.getShippingFee()).setScale(2, RoundingMode.HALF_UP);

        return new OrderResponse(
            order.getId(),
            order.getBuyerId(),
            order.getStatus(),
            order.getPaymentMethod(),
            itemsSubtotal,
            order.getShippingFee(),
            totalAmount,
            addressResponse,
            itemResponses,
            order.getDeliveredAt(),
            order.getCreatedAt(),
            order.getUpdatedAt()
        );
    }
}
