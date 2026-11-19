package com.nsv.backend.order.service;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.inventory.entity.Batch;
import com.nsv.backend.inventory.repository.BatchRepository;
import com.nsv.backend.order.dto.*;
import com.nsv.backend.order.entity.Order;
import com.nsv.backend.order.entity.OrderAddress;
import com.nsv.backend.order.entity.OrderItem;
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
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PreOrderServiceImpl implements PreOrderService {

    private static final BigDecimal MIN_DEPOSIT_RATE = new BigDecimal("0.20");
    private static final BigDecimal MAX_DEPOSIT_RATE = new BigDecimal("0.30");
    private static final BigDecimal DEFAULT_DEPOSIT_RATE = new BigDecimal("0.30");

    private final BatchRepository batchRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final FinancialTransactionService financialTransactionService;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public PreOrderResponse createPreOrder(Long buyerId, CreatePreOrderRequest request) {
        // 1. Xác thực tỷ lệ đặt cọc theo quy định (20% - 30%)
        BigDecimal effectiveRate = (request.depositRate() != null)
            ? request.depositRate().setScale(4, RoundingMode.HALF_UP)
            : DEFAULT_DEPOSIT_RATE.setScale(4, RoundingMode.HALF_UP);

        if (effectiveRate.compareTo(MIN_DEPOSIT_RATE) < 0 || effectiveRate.compareTo(MAX_DEPOSIT_RATE) > 0) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "INVALID_DEPOSIT_RATE",
                "Tỷ lệ đặt cọc Pre-order phải nằm trong khoảng quy định từ 20% đến 30%"
            );
        }

        // 2. Xác thực sổ địa chỉ và lấy tên tỉnh/thành phố
        Address address = addressRepository.findByIdAndUser(request.addressId(), buyerId)
            .orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND,
                "ADDRESS_NOT_FOUND",
                "Không tìm thấy địa chỉ nhận hàng"
            ));
        String provinceName = resolveProvinceName(address.getProvince().getId());

        // 3. Khóa bi quan lô hàng (SELECT ... FOR UPDATE) để giữ chỗ tồn kho đặt trước
        List<Batch> lockedBatches = batchRepository.findAllByIdInOrderByIdAscForUpdate(List.of(request.batchId()));
        if (lockedBatches.isEmpty()) {
            throw new BusinessException(
                HttpStatus.NOT_FOUND,
                "BATCH_NOT_FOUND",
                "Không tìm thấy lô sản phẩm đặt trước"
            );
        }

        Batch batch = lockedBatches.get(0);
        if (!"PRE_ORDER".equals(batch.getStatus())) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "BATCH_NOT_PRE_ORDER",
                "Lô sản phẩm này không thuộc chương trình đặt trước (trạng thái hiện tại: " + batch.getStatus() + ")"
            );
        }
        if (batch.getHarvestDate() == null) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "MISSING_HARVEST_SCHEDULE",
                "Sản phẩm chưa được gắn lịch thu hoạch dự kiến nên không thể đặt trước"
            );
        }
        if (batch.getRemainingQuantity().compareTo(request.quantity()) < 0) {
            throw new BusinessException(
                HttpStatus.CONFLICT,
                "INSUFFICIENT_PRE_ORDER_QUOTA",
                "Số lượng đặt trước vượt quá sản lượng dự kiến còn lại (" + batch.getRemainingQuantity() + ")"
            );
        }

        // 4. Tính toán chính xác tổng tiền đơn hàng, tiền cọc bắt buộc và tiền còn lại
        BigDecimal shippingFee = (request.shippingFee() != null ? request.shippingFee() : BigDecimal.ZERO)
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal itemsSubtotal = batch.getUnitPrice()
            .multiply(request.quantity())
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = itemsSubtotal.add(shippingFee).setScale(2, RoundingMode.HALF_UP);

        BigDecimal requiredDeposit = totalAmount.multiply(effectiveRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal remainingAmount = totalAmount.subtract(requiredDeposit).setScale(2, RoundingMode.HALF_UP);

        // 5. Trừ sản lượng giữ chỗ trên lô PRE_ORDER
        BigDecimal newRemainingStock = batch.getRemainingQuantity().subtract(request.quantity());
        batch.setRemainingQuantity(newRemainingStock);
        if (newRemainingStock.compareTo(BigDecimal.ZERO) == 0) {
            batch.setStatus("SOLD_OUT");
        }
        batchRepository.save(batch);

        // 6. Tạo đơn hàng Pre-order với trạng thái riêng biệt AWAITING_DEPOSIT
        Order order = Order.builder()
            .buyerId(buyerId)
            .addressId(address.getId())
            .orderType("PRE_ORDER")
            .paymentMethod("ONLINE")
            .status("AWAITING_DEPOSIT")
            .shippingFee(shippingFee)
            .depositRate(effectiveRate)
            .depositAmount(requiredDeposit)
            .remainingAmount(remainingAmount)
            .expectedHarvestDate(batch.getHarvestDate())
            .build();

        order.attachAddress(OrderAddress.builder()
            .receiverName(address.getReceiverName())
            .receiverPhone(address.getReceiverPhone())
            .provinceName(provinceName)
            .addressLine(address.getAddressLine())
            .build());

        order.addItem(OrderItem.builder()
            .batchId(batch.getId())
            .quantity(request.quantity())
            .unitPrice(batch.getUnitPrice())
            .build());

        Order savedOrder = orderRepository.save(order);

        notifyFarmersNewPreOrder(savedOrder);
        recordAuditLog(buyerId, "CREATE_PRE_ORDER", "orders", savedOrder.getId());

        return mapToPreOrderResponse(savedOrder, Map.of(batch.getId(), batch));
    }

    @Override
    @Transactional
    public PreOrderResponse payPreOrderDeposit(Long buyerId, Long orderId, PayDepositRequest request) {
        Order order = orderRepository.findWithDetailsByIdAndBuyerId(orderId, buyerId)
            .orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND,
                "PRE_ORDER_NOT_FOUND",
                "Không tìm thấy đơn đặt trước"
            ));

        if (!"PRE_ORDER".equals(order.getOrderType())) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "NOT_A_PRE_ORDER",
                "Đơn hàng này là đơn thông thường, không áp dụng quy trình đặt cọc Pre-order"
            );
        }
        if (!"AWAITING_DEPOSIT".equals(order.getStatus())) {
            throw new BusinessException(
                HttpStatus.CONFLICT,
                "INVALID_PRE_ORDER_STATUS",
                "Đơn đặt trước không ở trạng thái chờ đặt cọc (trạng thái hiện tại: " + order.getStatus() + ")"
            );
        }

        // Thực hiện giao dịch đặt cọc qua FinancialTransactionService (đảm bảo chính xác & chống trùng lặp)
        String providerRef = request != null ? request.providerRef() : null;
        PaymentResponse depositPayment = financialTransactionService.processPayment(
            buyerId,
            false,
            new ProcessPaymentRequest(order.getId(), "DEPOSIT", order.getDepositRate(), providerRef)
        );

        // Cập nhật trạng thái riêng của đơn Pre-order sau khi cọc thành công
        order.setDepositAmount(depositPayment.amount());
        order.setRemainingAmount(depositPayment.orderTotal().subtract(depositPayment.amount()).setScale(2, RoundingMode.HALF_UP));
        order.setStatus("DEPOSIT_PAID");

        Order savedOrder = orderRepository.save(order);

        notifyDepositConfirmed(savedOrder);
        recordAuditLog(buyerId, "PAY_PRE_ORDER_DEPOSIT", "orders", savedOrder.getId());

        return mapSinglePreOrder(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PreOrderResponse> getBuyerPreOrders(Long buyerId) {
        List<Order> preOrders = orderRepository.findAllByBuyerIdAndOrderTypeOrderByCreatedAtDesc(buyerId, "PRE_ORDER");
        if (preOrders.isEmpty()) {
            return List.of();
        }

        Set<Long> batchIds = preOrders.stream()
            .flatMap(o -> o.getItems().stream())
            .map(OrderItem::getBatchId)
            .collect(Collectors.toSet());

        Map<Long, Batch> batchMap = batchIds.isEmpty()
            ? Map.of()
            : batchRepository.findAllByIdIn(batchIds).stream()
                .collect(Collectors.toMap(Batch::getId, Function.identity()));

        return preOrders.stream()
            .map(order -> mapToPreOrderResponse(order, batchMap))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PreOrderResponse getBuyerPreOrderDetail(Long buyerId, Long orderId) {
        Order order = orderRepository.findWithDetailsByIdAndBuyerId(orderId, buyerId)
            .orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND,
                "PRE_ORDER_NOT_FOUND",
                "Không tìm thấy đơn đặt trước"
            ));

        if (!"PRE_ORDER".equals(order.getOrderType())) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "NOT_A_PRE_ORDER",
                "Đơn hàng yêu cầu không phải đơn đặt trước (PRE_ORDER)"
            );
        }

        return mapSinglePreOrder(order);
    }

    private PreOrderResponse mapSinglePreOrder(Order order) {
        Set<Long> batchIds = order.getItems().stream()
            .map(OrderItem::getBatchId)
            .collect(Collectors.toSet());

        Map<Long, Batch> batchMap = batchIds.isEmpty()
            ? Map.of()
            : batchRepository.findAllByIdIn(batchIds).stream()
                .collect(Collectors.toMap(Batch::getId, Function.identity()));

        return mapToPreOrderResponse(order, batchMap);
    }

    private PreOrderResponse mapToPreOrderResponse(Order order, Map<Long, Batch> batchMap) {
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

        BigDecimal totalAmount = itemsSubtotal.add(order.getShippingFee()).setScale(2, RoundingMode.HALF_UP);
        OrderAddress addr = order.getOrderAddress();
        OrderAddressResponse addressResponse = addr != null
            ? new OrderAddressResponse(
                addr.getReceiverName(),
                addr.getReceiverPhone(),
                addr.getProvinceName(),
                addr.getAddressLine()
            )
            : null;

        boolean depositPaid = !"AWAITING_DEPOSIT".equals(order.getStatus()) && !"CANCELLED".equals(order.getStatus());

        return new PreOrderResponse(
            order.getId(),
            order.getBuyerId(),
            order.getOrderType(),
            "Đơn đặt trước mùa vụ (Pre-order)",
            order.getStatus(),
            resolvePreOrderStatusLabel(order.getStatus(), order.getExpectedHarvestDate()),
            order.getPaymentMethod(),
            order.getExpectedHarvestDate(),
            itemsSubtotal,
            order.getShippingFee(),
            totalAmount,
            order.getDepositRate(),
            order.getDepositAmount(),
            order.getRemainingAmount(),
            depositPaid,
            addressResponse,
            itemResponses,
            order.getCreatedAt(),
            order.getUpdatedAt()
        );
    }

    private String resolvePreOrderStatusLabel(String status, LocalDate harvestDate) {
        String dateSuffix = harvestDate != null ? " (Dự kiến thu hoạch: " + harvestDate + ")" : "";
        return switch (status) {
            case "AWAITING_DEPOSIT" -> "Đặt trước - Chờ thanh toán tiền cọc giữ chỗ";
            case "DEPOSIT_PAID" -> "Đặt trước - Đã đặt cọc giữ chỗ" + dateSuffix;
            case "CONFIRMED" -> "Đặt trước - Đã thu hoạch & đang chuẩn bị hàng";
            case "SHIPPING" -> "Đặt trước - Đang giao hàng";
            case "DELIVERED" -> "Đặt trước - Đã giao hàng";
            case "COMPLETED" -> "Đặt trước - Hoàn tất";
            case "CANCELLED" -> "Đặt trước - Đã hủy";
            default -> status;
        };
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

    private void notifyFarmersNewPreOrder(Order order) {
        for (Long farmerId : orderRepository.findDistinctFarmerIdsByOrderId(order.getId())) {
            jdbcTemplate.update(
                "INSERT INTO notifications (user_id, content, is_read) VALUES (?, ?, FALSE)",
                farmerId,
                String.format(
                    "Có đơn đặt trước mới #%d (Lịch thu hoạch: %s). Đang chờ người mua thanh toán tiền cọc %s VND.",
                    order.getId(), order.getExpectedHarvestDate(), order.getDepositAmount()
                )
            );
        }
    }

    private void notifyDepositConfirmed(Order order) {
        jdbcTemplate.update(
            "INSERT INTO notifications (user_id, content, is_read) VALUES (?, ?, FALSE)",
            order.getBuyerId(),
            String.format(
                "Đơn đặt trước #%d đã thanh toán cọc %s VND thành công. Suất hàng của bạn đã được giữ chỗ đến ngày thu hoạch %s.",
                order.getId(), order.getDepositAmount(), order.getExpectedHarvestDate()
            )
        );
        for (Long farmerId : orderRepository.findDistinctFarmerIdsByOrderId(order.getId())) {
            jdbcTemplate.update(
                "INSERT INTO notifications (user_id, content, is_read) VALUES (?, ?, FALSE)",
                farmerId,
                String.format(
                    "Đơn đặt trước #%d đã hoàn tất đặt cọc giữ chỗ (%s VND). Vui lòng chuẩn bị sản lượng cho ngày thu hoạch %s.",
                    order.getId(), order.getDepositAmount(), order.getExpectedHarvestDate()
                )
            );
        }
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
}
