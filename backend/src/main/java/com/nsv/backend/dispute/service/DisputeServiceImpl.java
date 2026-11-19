package com.nsv.backend.dispute.service;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.dispute.dto.*;
import com.nsv.backend.dispute.entity.Complaint;
import com.nsv.backend.dispute.entity.ComplaintEvidence;
import com.nsv.backend.dispute.repository.ComplaintRepository;
import com.nsv.backend.inventory.entity.Batch;
import com.nsv.backend.inventory.repository.BatchRepository;
import com.nsv.backend.order.dto.CoInspectionResponse;
import com.nsv.backend.order.dto.PaymentResponse;
import com.nsv.backend.order.dto.ProcessRefundRequest;
import com.nsv.backend.order.dto.RefundResponse;
import com.nsv.backend.order.entity.Order;
import com.nsv.backend.order.entity.OrderItem;
import com.nsv.backend.order.entity.Payment;
import com.nsv.backend.order.entity.Refund;
import com.nsv.backend.order.repository.OrderCoInspectionRepository;
import com.nsv.backend.order.repository.OrderRepository;
import com.nsv.backend.order.repository.PaymentRepository;
import com.nsv.backend.order.repository.RefundRepository;
import com.nsv.backend.order.service.FinancialTransactionService;
import com.nsv.backend.product.entity.Product;
import com.nsv.backend.product.repository.ProductRepository;
import com.nsv.backend.trace.dto.FarmingLogResponse;
import com.nsv.backend.trace.repository.FarmingLogRepository;
import lombok.RequiredArgsConstructor;
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
public class DisputeServiceImpl implements DisputeService {

    private static final BigDecimal ZERO_MONEY = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final Set<String> VALID_DISPUTE_STATUSES = Set.of(
        "OPEN", "IN_REVIEW", "RESOLVED_REFUND", "RESOLVED_REJECTED"
    );

    private final ComplaintRepository complaintRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final BatchRepository batchRepository;
    private final ProductRepository productRepository;
    private final FarmingLogRepository farmingLogRepository;
    private final OrderCoInspectionRepository coInspectionRepository;
    private final FinancialTransactionService financialTransactionService;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public DisputeDetailResponse createComplaint(Long buyerId, Long orderId, CreateComplaintRequest request) {
        Order order = orderRepository.findWithDetailsById(orderId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Không tìm thấy đơn hàng"));

        if (!order.getBuyerId().equals(buyerId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "ORDER_ACCESS_DENIED", "Bạn không có quyền khiếu nại đơn hàng này");
        }
        if ("PENDING".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "INVALID_ORDER_STATUS_FOR_COMPLAINT",
                "Không thể tạo khiếu nại cho đơn hàng ở trạng thái " + order.getStatus()
            );
        }
        if (complaintRepository.existsByOrderId(orderId)) {
            throw new BusinessException(HttpStatus.CONFLICT, "COMPLAINT_ALREADY_EXISTS", "Đơn hàng này đã có khiếu nại trước đó");
        }

        Complaint complaint = Complaint.builder()
            .orderId(order.getId())
            .buyerId(buyerId)
            .reason(request.reason().trim())
            .description(request.description().trim())
            .status("OPEN")
            .refundAmount(ZERO_MONEY)
            .build();

        for (EvidenceItemRequest item : request.evidences()) {
            complaint.addEvidence(ComplaintEvidence.builder()
                .uploadedBy(buyerId)
                .uploaderRole("BUYER")
                .mediaType(item.mediaType().trim().toUpperCase())
                .mediaUrl(item.mediaUrl().trim())
                .caption(StringUtils.hasText(item.caption()) ? item.caption().trim() : null)
                .build());
        }

        Complaint saved = complaintRepository.save(complaint);

        for (Long farmerId : orderRepository.findDistinctFarmerIdsByOrderId(orderId)) {
            sendNotification(
                farmerId,
                String.format("Đơn hàng #%d có khiếu nại mới (#%d) từ người mua. Vui lòng gửi giải trình và bằng chứng đối chứng.", orderId, saved.getId())
            );
        }
        recordAuditLog(buyerId, "CREATE_COMPLAINT", "complaints", saved.getId());

        return buildFullDisputeDetail(saved, order);
    }

    @Override
    @Transactional
    public DisputeDetailResponse submitFarmerResponse(
            Long farmerId,
            Long complaintId,
            FarmerDisputeResponseRequest request
    ) {
        Complaint complaint = complaintRepository.findWithEvidencesById(complaintId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "COMPLAINT_NOT_FOUND", "Không tìm thấy khiếu nại"));

        if (!orderRepository.existsOrderForFarmer(complaint.getOrderId(), farmerId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "DISPUTE_ACCESS_DENIED", "Bạn không có quyền phản hồi tranh chấp này");
        }
        if ("RESOLVED_REFUND".equals(complaint.getStatus()) || "RESOLVED_REJECTED".equals(complaint.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "COMPLAINT_ALREADY_RESOLVED", "Tranh chấp đã được Admin phân xử");
        }

        complaint.setFarmerResponse(request.responseMessage().trim());
        if ("OPEN".equals(complaint.getStatus())) {
            complaint.setStatus("IN_REVIEW");
        }

        if (request.evidences() != null) {
            for (EvidenceItemRequest item : request.evidences()) {
                complaint.addEvidence(ComplaintEvidence.builder()
                    .uploadedBy(farmerId)
                    .uploaderRole("FARMER")
                    .mediaType(item.mediaType().trim().toUpperCase())
                    .mediaUrl(item.mediaUrl().trim())
                    .caption(StringUtils.hasText(item.caption()) ? item.caption().trim() : null)
                    .build());
            }
        }

        Complaint saved = complaintRepository.save(complaint);
        recordAuditLog(farmerId, "FARMER_RESPOND_COMPLAINT", "complaints", saved.getId());

        Order order = orderRepository.findWithDetailsById(saved.getOrderId())
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Không tìm thấy đơn hàng"));

        return buildFullDisputeDetail(saved, order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisputeDetailResponse> getAllDisputesForAdmin(String statusFilter) {
        List<Complaint> complaints;
        if (StringUtils.hasText(statusFilter)) {
            String normalized = statusFilter.trim().toUpperCase();
            if (!VALID_DISPUTE_STATUSES.contains(normalized)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_DISPUTE_STATUS", "Trạng thái lọc tranh chấp không hợp lệ");
            }
            complaints = complaintRepository.findAllByStatusOrderByCreatedAtDesc(normalized);
        } else {
            complaints = complaintRepository.findAllByOrderByCreatedAtDesc();
        }

        return complaints.stream()
            .map(c -> {
                Order order = orderRepository.findWithDetailsById(c.getOrderId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Không tìm thấy đơn hàng"));
                return buildFullDisputeDetail(c, order);
            })
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DisputeDetailResponse getDisputeDetailForAdmin(Long complaintId) {
        Complaint complaint = complaintRepository.findWithEvidencesById(complaintId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "COMPLAINT_NOT_FOUND", "Không tìm thấy hồ sơ tranh chấp"));

        Order order = orderRepository.findWithDetailsById(complaint.getOrderId())
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Không tìm thấy đơn hàng liên quan"));

        return buildFullDisputeDetail(complaint, order);
    }

    @Override
    @Transactional
    public DisputeDetailResponse resolveDisputeByAdmin(
            Long adminId,
            Long complaintId,
            ResolveDisputeRequest request
    ) {
        // 1. Khóa bi quan hồ sơ khiếu nại và đơn hàng để đảm bảo phân xử nguyên tử, không trùng lặp
        Complaint complaint = complaintRepository.findByIdForUpdate(complaintId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "COMPLAINT_NOT_FOUND", "Không tìm thấy hồ sơ tranh chấp"));

        if ("RESOLVED_REFUND".equals(complaint.getStatus()) || "RESOLVED_REJECTED".equals(complaint.getStatus())) {
            throw new BusinessException(
                HttpStatus.CONFLICT,
                "DISPUTE_ALREADY_RESOLVED",
                "Tranh chấp này đã được phân xử trước đó với trạng thái " + complaint.getStatus()
            );
        }

        Order order = orderRepository.findByIdForUpdate(complaint.getOrderId())
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Không tìm thấy đơn hàng liên quan"));

        String decision = request.decision().trim().toUpperCase();
        String adminNote = request.adminNote().trim();
        OffsetDateTime now = OffsetDateTime.now();

        BigDecimal totalRefundedInResolution = ZERO_MONEY;

        if ("FULL_REFUND".equals(decision) || "PARTIAL_REFUND".equals(decision)) {
            totalRefundedInResolution = executeDisputeRefund(
                adminId,
                complaint.getId(),
                order.getId(),
                decision,
                request.partialRefundAmount(),
                adminNote
            );

            complaint.setStatus("RESOLVED_REFUND");
            complaint.setRefundAmount(totalRefundedInResolution);

            // Cập nhật trạng thái đơn hàng tương ứng sau khi hoàn tiền
            if ("FULL_REFUND".equals(decision)) {
                order.setStatus("CANCELLED");
            } else if ("DELIVERED".equals(order.getStatus()) || "SHIPPING".equals(order.getStatus())) {
                order.setStatus("COMPLETED");
            }
        } else if ("REJECT_COMPLAINT".equals(decision)) {
            complaint.setStatus("RESOLVED_REJECTED");
            complaint.setRefundAmount(ZERO_MONEY);

            if ("DELIVERED".equals(order.getStatus())) {
                order.setStatus("COMPLETED");
            }
        }

        complaint.setAdminDecision(decision);
        complaint.setAdminNote(adminNote);
        complaint.setResolvedBy(adminId);
        complaint.setResolvedAt(now);

        Order savedOrder = orderRepository.save(order);
        Complaint savedComplaint = complaintRepository.save(complaint);

        notifyDisputeParties(savedComplaint, savedOrder);
        recordAuditLog(adminId, "RESOLVE_DISPUTE_" + decision, "complaints", savedComplaint.getId());

        Order detailedOrder = orderRepository.findWithDetailsById(savedOrder.getId()).orElse(savedOrder);
        Complaint detailedComplaint = complaintRepository.findWithEvidencesById(savedComplaint.getId()).orElse(savedComplaint);
        return buildFullDisputeDetail(detailedComplaint, detailedOrder);
    }

    private BigDecimal executeDisputeRefund(
            Long adminId,
            Long complaintId,
            Long orderId,
            String decision,
            BigDecimal partialRefundAmount,
            String adminNote
    ) {
        List<Payment> payments = paymentRepository.findAllByOrderIdOrderByCreatedAtAsc(orderId);
        List<Payment> paidPayments = payments.stream()
            .filter(p -> "PAID".equals(p.getStatus()))
            .toList();

        if (paidPayments.isEmpty()) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "NO_PAID_PAYMENT_TO_REFUND",
                "Đơn hàng chưa có giao dịch thanh toán thành công để thực hiện hoàn tiền"
            );
        }

        Set<Long> paidIds = paidPayments.stream().map(Payment::getId).collect(Collectors.toSet());
        Map<Long, BigDecimal> alreadyRefundedByPayment = refundRepository
            .findAllByPaymentIdInOrderByCreatedAtAsc(paidIds)
            .stream()
            .filter(r -> "COMPLETED".equals(r.getStatus()) || "PENDING".equals(r.getStatus()))
            .collect(Collectors.groupingBy(
                Refund::getPaymentId,
                Collectors.reducing(ZERO_MONEY, Refund::getAmount, BigDecimal::add)
            ));

        BigDecimal totalAvailableToRefund = ZERO_MONEY;
        for (Payment p : paidPayments) {
            BigDecimal already = alreadyRefundedByPayment.getOrDefault(p.getId(), ZERO_MONEY);
            totalAvailableToRefund = totalAvailableToRefund.add(p.getAmount().subtract(already));
        }
        totalAvailableToRefund = totalAvailableToRefund.setScale(2, RoundingMode.HALF_UP);

        if (totalAvailableToRefund.compareTo(ZERO_MONEY) <= 0) {
            throw new BusinessException(
                HttpStatus.CONFLICT,
                "NO_REFUNDABLE_BALANCE",
                "Đơn hàng không còn số dư khả dụng để hoàn tiền"
            );
        }

        if ("FULL_REFUND".equals(decision)) {
            BigDecimal totalRefunded = ZERO_MONEY;
            for (Payment p : paidPayments) {
                BigDecimal already = alreadyRefundedByPayment.getOrDefault(p.getId(), ZERO_MONEY);
                BigDecimal remaining = p.getAmount().subtract(already).setScale(2, RoundingMode.HALF_UP);
                if (remaining.compareTo(ZERO_MONEY) > 0) {
                    RefundResponse res = financialTransactionService.processRefund(
                        adminId,
                        new ProcessRefundRequest(
                            p.getId(),
                            complaintId,
                            remaining,
                            "Admin phân xử hoàn tiền toàn bộ tranh chấp #" + complaintId + ": " + adminNote
                        )
                    );
                    totalRefunded = totalRefunded.add(res.amount());
                }
            }
            return totalRefunded.setScale(2, RoundingMode.HALF_UP);
        }

        // PARTIAL_REFUND
        if (partialRefundAmount == null || partialRefundAmount.compareTo(ZERO_MONEY) <= 0) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "INVALID_PARTIAL_REFUND_AMOUNT",
                "Vui lòng nhập số tiền hoàn một phần hợp lệ (> 0)"
            );
        }
        BigDecimal targetRefund = partialRefundAmount.setScale(2, RoundingMode.HALF_UP);
        if (targetRefund.compareTo(totalAvailableToRefund) > 0) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "REFUND_EXCEEDS_PAID_AMOUNT",
                "Số tiền hoàn (" + targetRefund + ") vượt quá tổng số tiền đã thanh toán (" + totalAvailableToRefund + ")"
            );
        }

        BigDecimal remainingToAllocate = targetRefund;
        BigDecimal totalRefunded = ZERO_MONEY;

        for (Payment p : paidPayments) {
            if (remainingToAllocate.compareTo(ZERO_MONEY) <= 0) {
                break;
            }
            BigDecimal already = alreadyRefundedByPayment.getOrDefault(p.getId(), ZERO_MONEY);
            BigDecimal availableOnThisPayment = p.getAmount().subtract(already).setScale(2, RoundingMode.HALF_UP);
            if (availableOnThisPayment.compareTo(ZERO_MONEY) <= 0) {
                continue;
            }

            BigDecimal chargeRefund = remainingToAllocate.min(availableOnThisPayment);
            RefundResponse res = financialTransactionService.processRefund(
                adminId,
                new ProcessRefundRequest(
                    p.getId(),
                    complaintId,
                    chargeRefund,
                    "Admin phân xử hoàn tiền một phần tranh chấp #" + complaintId + ": " + adminNote
                )
            );
            totalRefunded = totalRefunded.add(res.amount());
            remainingToAllocate = remainingToAllocate.subtract(res.amount());
        }

        return totalRefunded.setScale(2, RoundingMode.HALF_UP);
    }

    private DisputeDetailResponse buildFullDisputeDetail(Complaint complaint, Order order) {
        // 1. Bằng chứng ảnh/video từ Buyer và Farmer
        List<ComplaintEvidenceResponse> evidenceResponses = complaint.getEvidences().stream()
            .map(ComplaintEvidenceResponse::from)
            .toList();

        // 2. Nhật ký trồng trọt (farming_logs) của từng lô hàng trong đơn
        Set<Long> batchIds = order.getItems().stream()
            .map(OrderItem::getBatchId)
            .collect(Collectors.toSet());

        Map<Long, Batch> batchMap = batchIds.isEmpty()
            ? Map.of()
            : batchRepository.findAllByIdIn(batchIds).stream()
                .collect(Collectors.toMap(Batch::getId, Function.identity()));

        Set<Long> productIds = batchMap.values().stream()
            .map(Batch::getProductId)
            .collect(Collectors.toSet());

        Map<Long, Product> productMap = productIds.isEmpty()
            ? Map.of()
            : productRepository.findAllByIdIn(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        List<BatchFarmingLogSummary> farmingLogSummaries = batchIds.stream()
            .sorted()
            .map(batchId -> {
                Batch batch = batchMap.get(batchId);
                Product product = batch != null ? productMap.get(batch.getProductId()) : null;
                List<FarmingLogResponse> logs = farmingLogRepository
                    .findAllByBatchIdOrderByLogDateAscIdAsc(batchId)
                    .stream()
                    .map(FarmingLogResponse::from)
                    .toList();

                return new BatchFarmingLogSummary(
                    batchId,
                    batch != null ? batch.getProductId() : null,
                    product != null ? product.getName() : null,
                    batch != null ? batch.getTraceabilityCode() : null,
                    batch != null ? batch.getHarvestDate() : null,
                    logs
                );
            })
            .toList();

        // 3. Thông tin đồng kiểm (nếu có)
        CoInspectionResponse coInspection = coInspectionRepository.findByOrderId(order.getId())
            .map(ci -> CoInspectionResponse.from(ci, order.getStatus()))
            .orElse(null);

        // 4. Lịch sử giao dịch thanh toán & hoàn tiền
        BigDecimal orderTotal = calculateExactOrderTotal(order);
        List<Payment> payments = paymentRepository.findAllByOrderIdOrderByCreatedAtAsc(order.getId());
        BigDecimal totalPaid = payments.stream()
            .filter(p -> "PAID".equals(p.getStatus()) || "REFUNDED".equals(p.getStatus()))
            .map(Payment::getAmount)
            .reduce(ZERO_MONEY, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

        Set<Long> paymentIds = payments.stream().map(Payment::getId).collect(Collectors.toSet());
        List<Refund> refunds = paymentIds.isEmpty()
            ? List.of()
            : refundRepository.findAllByPaymentIdInOrderByCreatedAtAsc(paymentIds);

        // 5. Nhật ký thao tác hệ thống (audit_logs) liên quan đến đơn hàng và khiếu nại
        List<SystemAuditLogResponse> auditLogs = fetchRelatedAuditLogs(order.getId(), complaint.getId());

        return new DisputeDetailResponse(
            complaint.getId(),
            order.getId(),
            order.getStatus(),
            complaint.getBuyerId(),
            complaint.getReason(),
            complaint.getDescription(),
            complaint.getFarmerResponse(),
            complaint.getStatus(),
            complaint.getAdminDecision(),
            complaint.getAdminNote(),
            orderTotal,
            totalPaid,
            complaint.getRefundAmount(),
            complaint.getResolvedBy(),
            complaint.getResolvedAt(),
            complaint.getCreatedAt(),
            evidenceResponses,
            farmingLogSummaries,
            coInspection,
            auditLogs,
            payments.stream().map(p -> PaymentResponse.from(p, orderTotal)).toList(),
            refunds.stream().map(RefundResponse::from).toList()
        );
    }

    private BigDecimal calculateExactOrderTotal(Order order) {
        BigDecimal itemsSubtotal = ZERO_MONEY;
        for (OrderItem item : order.getItems()) {
            itemsSubtotal = itemsSubtotal.add(
                item.getUnitPrice().multiply(item.getQuantity()).setScale(2, RoundingMode.HALF_UP)
            );
        }
        return itemsSubtotal.add(order.getShippingFee()).setScale(2, RoundingMode.HALF_UP);
    }

    private List<SystemAuditLogResponse> fetchRelatedAuditLogs(Long orderId, Long complaintId) {
        return jdbcTemplate.query(
            """
            SELECT id, actor_id, action, entity_type, entity_id, created_at
            FROM audit_logs
            WHERE (entity_type = 'orders' AND entity_id = ?)
               OR (entity_type = 'complaints' AND entity_id = ?)
            ORDER BY created_at ASC, id ASC
            """,
            (rs, rowNum) -> new SystemAuditLogResponse(
                rs.getLong("id"),
                rs.getLong("actor_id"),
                rs.getString("action"),
                rs.getString("entity_type"),
                rs.getLong("entity_id"),
                rs.getObject("created_at", OffsetDateTime.class)
            ),
            orderId,
            complaintId
        );
    }

    private void notifyDisputeParties(Complaint complaint, Order order) {
        String decisionSummary = switch (complaint.getAdminDecision()) {
            case "FULL_REFUND" -> "Chấp nhận hoàn tiền toàn bộ (" + complaint.getRefundAmount() + " VND)";
            case "PARTIAL_REFUND" -> "Chấp nhận hoàn tiền một phần (" + complaint.getRefundAmount() + " VND)";
            case "REJECT_COMPLAINT" -> "Từ chối khiếu nại, giữ nguyên thanh toán cho trang trại";
            default -> complaint.getAdminDecision();
        };

        String buyerMsg = String.format(
            "Admin đã phân xử khiếu nại #%d (Đơn hàng #%d): %s.",
            complaint.getId(), order.getId(), decisionSummary
        );
        sendNotification(complaint.getBuyerId(), buyerMsg);

        String farmerMsg = String.format(
            "Admin đã ra quyết định phân xử tranh chấp #%d trên đơn hàng #%d: %s.",
            complaint.getId(), order.getId(), decisionSummary
        );
        for (Long farmerId : orderRepository.findDistinctFarmerIdsByOrderId(order.getId())) {
            sendNotification(farmerId, farmerMsg);
        }
    }

    private void sendNotification(Long userId, String content) {
        jdbcTemplate.update(
            "INSERT INTO notifications (user_id, content, is_read) VALUES (?, ?, FALSE)",
            userId,
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
}
