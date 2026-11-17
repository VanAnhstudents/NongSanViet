package com.nsv.backend.order.service;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.inventory.entity.Batch;
import com.nsv.backend.inventory.repository.BatchRepository;
import com.nsv.backend.order.dto.CheckoutRequest;
import com.nsv.backend.order.dto.OrderAddressResponse;
import com.nsv.backend.order.dto.OrderItemResponse;
import com.nsv.backend.order.dto.OrderResponse;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final BatchRepository batchRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public OrderResponse checkoutFromCart(Long buyerId, CheckoutRequest request) {
        // 1. Lấy các sản phẩm từ giỏ hàng
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

        // 2. Xác thực sổ địa chỉ và lấy tên tỉnh/thành phố để chụp snapshot
        Address address = addressRepository.findByIdAndUserId(request.addressId(), buyerId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "ADDRESS_NOT_FOUND", "Không tìm thấy địa chỉ nhận hàng"));

        String provinceName = resolveProvinceName(address.getProvinceId());

        // 3. Khóa bi quan (SELECT ... FOR UPDATE theo thứ tự id tăng dần tránh deadlock) để kiểm tra & trừ tồn kho
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

        // 4. Khởi tạo đơn hàng và snapshot địa chỉ
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

        // 5. Trừ tồn kho từng lô và tạo chi tiết đơn hàng
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

        // 6. Xóa các mục đã đặt khỏi giỏ hàng
        cartItemRepository.deleteAll(targetItems);

        return toOrderResponse(savedOrder, lockedBatchMap);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetail(Long buyerId, Long orderId) {
        Order order = orderRepository.findWithDetailsByIdAndBuyerId(orderId, buyerId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Không tìm thấy đơn hàng"));

        Set<Long> batchIds = order.getItems().stream().map(OrderItem::getBatchId).collect(Collectors.toSet());
        Map<Long, Batch> batchMap = batchRepository.findAllByIdIn(batchIds).stream()
            .collect(Collectors.toMap(Batch::getId, Function.identity()));

        return toOrderResponse(order, batchMap);
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

    private OrderResponse toOrderResponse(Order order, Map<Long, Batch> batchMap) {
        Set<Long> productIds = batchMap.values().stream()
            .map(Batch::getProductId)
            .collect(Collectors.toSet());

        Map<Long, Product> productMap = productIds.isEmpty()
            ? Map.of()
            : productRepository.findAllByIdIn(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        BigDecimal itemsSubtotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        List<OrderItemResponse> itemResponses = new java.util.ArrayList<>(order.getItems().size());

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
            order.getCreatedAt()
        );
    }
}
