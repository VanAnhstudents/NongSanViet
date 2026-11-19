package com.nsv.backend.order.service;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.order.dto.CoInspectionResponse;
import com.nsv.backend.order.dto.ConfirmOrderDeliveryRequest;
import com.nsv.backend.order.dto.RecordCoInspectionRequest;
import com.nsv.backend.order.entity.Order;
import com.nsv.backend.order.entity.OrderCoInspection;
import com.nsv.backend.order.repository.OrderCoInspectionRepository;
import com.nsv.backend.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderCoInspectionServiceImpl implements OrderCoInspectionService {

    private static final Set<String> INSPECTABLE_ORDER_STATUSES = Set.of("SHIPPING", "DELIVERED");
    private static final Set<String> VALID_RESULTS = Set.of("PASSED", "FAILED");

    private final OrderRepository orderRepository;
    private final OrderCoInspectionRepository coInspectionRepository;
    private final JdbcTemplate jdbcTemplate;

    private Clock clock = Clock.systemDefaultZone();

    void setClock(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(this.clock);
    }

    @Override
    @Transactional
    public CoInspectionResponse recordCoInspection(
            Long buyerId,
            Long orderId,
            RecordCoInspectionRequest request
    ) {
        Order order = orderRepository.findByIdForUpdate(orderId)
            .orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND,
                "ORDER_NOT_FOUND",
                "Không tìm thấy đơn hàng"
            ));

        validateBuyerOwnershipAndOrderState(buyerId, order);

        String normalizedResult = request.result() != null ? request.result().trim().toUpperCase() : "";
        if (!VALID_RESULTS.contains(normalizedResult)) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "INVALID_CO_INSPECTION_RESULT",
                "Kết quả đồng kiểm chỉ hỗ trợ PASSED (Đạt) hoặc FAILED (Không đạt)"
            );
        }

        String sanitizedNotes = StringUtils.hasText(request.notes()) ? request.notes().trim() : null;
        if ("FAILED".equals(normalizedResult) && !StringUtils.hasText(sanitizedNotes)) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "CO_INSPECTION_NOTES_REQUIRED",
                "Vui lòng nhập ghi chú mô tả tình trạng hàng hóa khi kết quả đồng kiểm là Không đạt (FAILED)"
            );
        }

        OrderCoInspection existing = coInspectionRepository.findByOrderId(orderId).orElse(null);
        if (existing != null && existing.isLocked()) {
            throw new BusinessException(
                HttpStatus.CONFLICT,
                "CO_INSPECTION_LOCKED",
                "Kết quả đồng kiểm đã được xác nhận và khóa cùng đơn hàng hoàn tất, không thể chỉnh sửa lại"
            );
        }

        OrderCoInspection inspection = (existing != null)
            ? existing
            : OrderCoInspection.builder()
                .orderId(order.getId())
                .buyerId(buyerId)
                .build();

        inspection.setResult(normalizedResult);
        inspection.setNotes(sanitizedNotes);

        boolean confirmNow = request.shouldConfirmDelivery();
        if (confirmNow) {
            applyFinalConfirmation(buyerId, order, inspection);
        } else {
            inspection = coInspectionRepository.save(inspection);
            recordAuditLog(buyerId, "SAVE_CO_INSPECTION_DRAFT", "orders", order.getId());
        }

        return CoInspectionResponse.from(inspection, order.getStatus());
    }

    @Override
    @Transactional
    public CoInspectionResponse confirmDeliveryAndComplete(
            Long buyerId,
            Long orderId,
            ConfirmOrderDeliveryRequest request
    ) {
        Order order = orderRepository.findByIdForUpdate(orderId)
            .orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND,
                "ORDER_NOT_FOUND",
                "Không tìm thấy đơn hàng"
            ));

        validateBuyerOwnershipAndOrderState(buyerId, order);

        OrderCoInspection inspection = coInspectionRepository.findByOrderId(orderId)
            .orElseThrow(() -> new BusinessException(
                HttpStatus.BAD_REQUEST,
                "CO_INSPECTION_REQUIRED",
                "Vui lòng ghi nhận kết quả đồng kiểm (Đạt/Không đạt) trước khi xác nhận đã giao hàng"
            ));

        if (inspection.isLocked()) {
            throw new BusinessException(
                HttpStatus.CONFLICT,
                "CO_INSPECTION_LOCKED",
                "Đơn hàng đã được xác nhận hoàn tất trước đó, không thể thao tác lại"
            );
        }

        if (request != null && StringUtils.hasText(request.finalNotes())) {
            inspection.setNotes(request.finalNotes().trim());
        }

        applyFinalConfirmation(buyerId, order, inspection);
        return CoInspectionResponse.from(inspection, order.getStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public CoInspectionResponse getCoInspectionByOrder(Long actorId, String role, Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND,
                "ORDER_NOT_FOUND",
                "Không tìm thấy đơn hàng"
            ));

        boolean authorized = "ADMIN".equals(role)
            || ("BUYER".equals(role) && order.getBuyerId().equals(actorId))
            || ("FARMER".equals(role) && orderRepository.existsOrderForFarmer(orderId, actorId));

        if (!authorized) {
            throw new BusinessException(
                HttpStatus.FORBIDDEN,
                "CO_INSPECTION_ACCESS_DENIED",
                "Bạn không có quyền xem kết quả đồng kiểm của đơn hàng này"
            );
        }

        OrderCoInspection inspection = coInspectionRepository.findByOrderId(orderId)
            .orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND,
                "CO_INSPECTION_NOT_FOUND",
                "Đơn hàng này chưa có phiếu ghi nhận đồng kiểm"
            ));

        return CoInspectionResponse.from(inspection, order.getStatus());
    }

    private void validateBuyerOwnershipAndOrderState(Long buyerId, Order order) {
        if (!order.getBuyerId().equals(buyerId)) {
            throw new BusinessException(
                HttpStatus.FORBIDDEN,
                "ORDER_ACCESS_DENIED",
                "Bạn không có quyền thao tác đồng kiểm trên đơn hàng này"
            );
        }

        if ("COMPLETED".equals(order.getStatus())) {
            throw new BusinessException(
                HttpStatus.CONFLICT,
                "CO_INSPECTION_LOCKED",
                "Đơn hàng đã ở trạng thái Hoàn tất (COMPLETED), không thể chỉnh sửa kết quả đồng kiểm"
            );
        }

        if (!INSPECTABLE_ORDER_STATUSES.contains(order.getStatus())) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "INVALID_ORDER_STATUS_FOR_CO_INSPECTION",
                "Chỉ có thể đồng kiểm và xác nhận nhận hàng khi đơn ở trạng thái Đang giao (SHIPPING) hoặc Đã giao (DELIVERED). Trạng thái hiện tại: " + order.getStatus()
            );
        }
    }

    private void applyFinalConfirmation(Long buyerId, Order order, OrderCoInspection inspection) {
        OffsetDateTime confirmedTime = now();

        inspection.setLocked(true);
        inspection.setConfirmedAt(confirmedTime);
        coInspectionRepository.save(inspection);

        if (order.getDeliveredAt() == null) {
            order.setDeliveredAt(confirmedTime);
        }

        if (inspection.isPassed()) {
            order.setStatus("COMPLETED");
        } else {
            // Đồng kiểm KHÔNG ĐẠT: đánh dấu Đã giao (DELIVERED) để kích hoạt mốc 24h khiếu nại (FR-24)
            order.setStatus("DELIVERED");
        }
        orderRepository.save(order);

        notifyFarmersAfterCoInspection(order, inspection);
        recordAuditLog(
            buyerId,
            "CONFIRM_CO_INSPECTION_" + inspection.getResult(),
            "orders",
            order.getId()
        );
    }

    private void notifyFarmersAfterCoInspection(Order order, OrderCoInspection inspection) {
        String message = inspection.isPassed()
            ? String.format(
                "Người mua đã đồng kiểm ĐẠT và xác nhận hoàn tất đơn hàng #%d.",
                order.getId()
            )
            : String.format(
                "Người mua đã ghi nhận đồng kiểm KHÔNG ĐẠT cho đơn hàng #%d. Ghi chú: %s",
                order.getId(),
                inspection.getNotes()
            );

        for (Long farmerId : orderRepository.findDistinctFarmerIdsByOrderId(order.getId())) {
            jdbcTemplate.update(
                "INSERT INTO notifications (user_id, content, is_read) VALUES (?, ?, FALSE)",
                farmerId,
                message
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
