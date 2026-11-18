package com.nsv.backend.order.service;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.order.dto.CoInspectionResponse;
import com.nsv.backend.order.dto.CreateCoInspectionRequest;
import com.nsv.backend.order.entity.Order;
import com.nsv.backend.order.entity.OrderCoInspection;
import com.nsv.backend.order.repository.OrderCoInspectionRepository;
import com.nsv.backend.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoInspectionServiceImpl implements CoInspectionService {

    // Chỉ cho phép gửi yêu cầu đồng kiểm trước khi đơn hàng được giao / xác nhận nhận hàng
    private static final Set<String> ELIGIBLE_STATUSES_FOR_REQUEST = Set.of(
        "PENDING", "CONFIRMED", "SHIPPING"
    );

    private final OrderRepository orderRepository;
    private final OrderCoInspectionRepository coInspectionRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public CoInspectionResponse requestCoInspection(
            Long buyerId,
            Long orderId,
            CreateCoInspectionRequest request
    ) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND,
                "ORDER_NOT_FOUND",
                "Không tìm thấy đơn hàng"
            ));

        if (!order.getBuyerId().equals(buyerId)) {
            throw new BusinessException(
                HttpStatus.FORBIDDEN,
                "ORDER_ACCESS_DENIED",
                "Bạn không có quyền gửi yêu cầu đồng kiểm cho đơn hàng này"
            );
        }

        if (!ELIGIBLE_STATUSES_FOR_REQUEST.contains(order.getStatus())) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "INVALID_ORDER_STATUS_FOR_CO_INSPECTION",
                "Chỉ có thể gửi yêu cầu đồng kiểm trước khi nhận hàng (trạng thái hiện tại: " + order.getStatus() + ")"
            );
        }

        if (coInspectionRepository.existsByOrderId(orderId)) {
            throw new BusinessException(
                HttpStatus.CONFLICT,
                "CO_INSPECTION_ALREADY_REQUESTED",
                "Đơn hàng này đã được đăng ký yêu cầu đồng kiểm trước đó"
            );
        }

        boolean checkQuantity = request.checkQuantity() == null || request.checkQuantity();
        boolean checkQuality = request.checkQuality() == null || request.checkQuality();
        if (!checkQuantity && !checkQuality) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "INVALID_CO_INSPECTION_CRITERIA",
                "Vui lòng chọn ít nhất một nội dung đồng kiểm (số lượng hoặc chất lượng)"
            );
        }

        OrderCoInspection coInspection = OrderCoInspection.builder()
            .orderId(order.getId())
            .buyerId(buyerId)
            .checkQuantity(checkQuantity)
            .checkQuality(checkQuality)
            .note(request.note() != null && !request.note().isBlank() ? request.note().trim() : null)
            .status("REQUESTED")
            .build();

        OrderCoInspection saved = coInspectionRepository.save(coInspection);

        notifyFarmersForCoInspection(order.getId());
        recordAuditLog(buyerId, "REQUEST_CO_INSPECTION", "order_co_inspections", saved.getId());

        return CoInspectionResponse.from(saved, order.getStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public CoInspectionResponse getOrderCoInspection(Long actorId, String role, Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND,
                "ORDER_NOT_FOUND",
                "Không tìm thấy đơn hàng"
            ));

        boolean isAllowed = "ADMIN".equals(role)
            || ("BUYER".equals(role) && order.getBuyerId().equals(actorId))
            || ("FARMER".equals(role) && orderRepository.existsOrderForFarmer(orderId, actorId));

        if (!isAllowed) {
            throw new BusinessException(
                HttpStatus.FORBIDDEN,
                "ORDER_ACCESS_DENIED",
                "Bạn không có quyền xem thông tin đồng kiểm của đơn hàng này"
            );
        }

        OrderCoInspection coInspection = coInspectionRepository.findByOrderId(orderId)
            .orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND,
                "CO_INSPECTION_NOT_FOUND",
                "Đơn hàng này chưa có yêu cầu đồng kiểm"
            ));

        return CoInspectionResponse.from(coInspection, order.getStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoInspectionResponse> getFarmerCoInspections(Long farmerId) {
        List<OrderCoInspection> list = coInspectionRepository.findAllByFarmerId(farmerId);
        if (list.isEmpty()) {
            return List.of();
        }

        Set<Long> orderIds = list.stream()
            .map(OrderCoInspection::getOrderId)
            .collect(Collectors.toSet());

        Map<Long, String> orderStatusMap = orderRepository.findAllById(orderIds).stream()
            .collect(Collectors.toMap(Order::getId, Order::getStatus));

        return list.stream()
            .map(ci -> CoInspectionResponse.from(ci, orderStatusMap.get(ci.getOrderId())))
            .toList();
    }

    @Override
    @Transactional
    public CoInspectionResponse acknowledgeCoInspection(Long farmerId, Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND,
                "ORDER_NOT_FOUND",
                "Không tìm thấy đơn hàng"
            ));

        if (!orderRepository.existsOrderForFarmer(orderId, farmerId)) {
            throw new BusinessException(
                HttpStatus.FORBIDDEN,
                "ORDER_ACCESS_DENIED",
                "Bạn không có quyền thao tác trên đơn hàng này"
            );
        }

        OrderCoInspection coInspection = coInspectionRepository.findByOrderId(orderId)
            .orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND,
                "CO_INSPECTION_NOT_FOUND",
                "Đơn hàng này chưa có yêu cầu đồng kiểm"
            ));

        if (!"REQUESTED".equals(coInspection.getStatus())) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "CO_INSPECTION_ALREADY_PROCESSED",
                "Yêu cầu đồng kiểm đã ở trạng thái " + coInspection.getStatus()
            );
        }

        coInspection.setStatus("ACKNOWLEDGED");
        coInspection.setAcknowledgedBy(farmerId);
        coInspection.setAcknowledgedAt(OffsetDateTime.now());

        OrderCoInspection saved = coInspectionRepository.save(coInspection);

        jdbcTemplate.update(
            "INSERT INTO notifications (user_id, content, is_read) VALUES (?, ?, FALSE)",
            order.getBuyerId(),
            String.format("Yêu cầu đồng kiểm cho đơn hàng #%d đã được trang trại tiếp nhận và ghi chú cho đơn vị giao hàng.", order.getId())
        );
        recordAuditLog(farmerId, "ACKNOWLEDGE_CO_INSPECTION", "order_co_inspections", saved.getId());

        return CoInspectionResponse.from(saved, order.getStatus());
    }

    private void notifyFarmersForCoInspection(Long orderId) {
        List<Long> farmerIds = orderRepository.findDistinctFarmerIdsByOrderId(orderId);
        String content = String.format(
            "Đơn hàng #%d có yêu cầu ĐỒNG KIỂM KHI NHẬN HÀNG từ người mua. Vui lòng ghi chú cho đơn vị vận chuyển phối hợp.",
            orderId
        );
        for (Long farmerId : farmerIds) {
            jdbcTemplate.update(
                "INSERT INTO notifications (user_id, content, is_read) VALUES (?, ?, FALSE)",
                farmerId,
                content
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
