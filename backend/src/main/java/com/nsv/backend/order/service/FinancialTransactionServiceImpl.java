package com.nsv.backend.order.service;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.order.dto.*;
import com.nsv.backend.order.entity.Order;
import com.nsv.backend.order.entity.OrderItem;
import com.nsv.backend.order.entity.Payment;
import com.nsv.backend.order.entity.Refund;
import com.nsv.backend.order.repository.OrderRepository;
import com.nsv.backend.order.repository.PaymentRepository;
import com.nsv.backend.order.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinancialTransactionServiceImpl implements FinancialTransactionService {

    private static final BigDecimal DEFAULT_DEPOSIT_RATE = new BigDecimal("0.30");
    private static final BigDecimal ZERO_MONEY = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public PaymentResponse processPayment(Long actorId, boolean isAdmin, ProcessPaymentRequest request) {
        // 1. Khóa bi quan dòng đơn hàng (SELECT ... FOR UPDATE) để tuần tự hóa các request đồng thời
        Order order = orderRepository.findByIdForUpdate(request.orderId())
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Không tìm thấy đơn hàng"));

        if (!isAdmin && !order.getBuyerId().equals(actorId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "ORDER_ACCESS_DENIED", "Bạn không có quyền thanh toán đơn hàng này");
        }
        if ("CANCELLED".equals(order.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "ORDER_CANCELLED", "Không thể thanh toán đơn hàng đã hủy");
        }

        if (StringUtils.hasText(request.providerRef()) && paymentRepository.existsByProviderRef(request.providerRef().trim())) {
            throw new BusinessException(HttpStatus.CONFLICT, "DUPLICATE_PROVIDER_REF", "Mã tham chiếu giao dịch đã được xử lý trước đó");
        }

        // 2. Tính tổng giá trị đơn hàng chính xác từ chi tiết đơn + phí ship
        BigDecimal orderTotal = calculateExactOrderTotal(order);
        if (orderTotal.compareTo(ZERO_MONEY) <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_ORDER_TOTAL", "Tổng tiền đơn hàng phải lớn hơn 0");
        }

        // 3. Kiểm tra lịch sử giao dịch đã thanh toán thành công trên đơn hàng
        List<Payment> existingPayments = paymentRepository.findAllByOrderIdOrderByCreatedAtAsc(order.getId());
        List<Payment> settledPayments = existingPayments.stream()
            .filter(p -> "PAID".equals(p.getStatus()) || "REFUNDED".equals(p.getStatus()))
            .toList();

        BigDecimal totalAlreadySettled = settledPayments.stream()
            .map(Payment::getAmount)
            .reduce(ZERO_MONEY, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

        String paymentType = request.paymentType().trim().toUpperCase();
        BigDecimal exactChargeAmount = resolveExactChargeAmount(
            paymentType,
            orderTotal,
            totalAlreadySettled,
            settledPayments,
            request.depositRate()
        );

        String providerRef = StringUtils.hasText(request.providerRef())
            ? request.providerRef().trim()
            : "SIM-PAY-" + order.getId() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Payment payment = Payment.builder()
            .orderId(order.getId())
            .paymentType(paymentType)
            .amount(exactChargeAmount)
            .status("PAID")
            .providerRef(providerRef)
            .build();

        Payment savedPayment = paymentRepository.save(payment);
        recordAuditLog(actorId, "PROCESS_PAYMENT_" + paymentType, "payments", savedPayment.getId());

        return PaymentResponse.from(savedPayment, orderTotal);
    }

    @Override
    @Transactional
    public RefundResponse processRefund(Long adminId, ProcessRefundRequest request) {
        // 1. Khóa bi quan dòng giao dịch gốc (SELECT ... FOR UPDATE) để chặn hoàn tiền trùng lặp khi chạy đồng thời
        Payment payment = paymentRepository.findByIdForUpdate(request.paymentId())
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND", "Không tìm thấy giao dịch thanh toán"));

        if ("REFUNDED".equals(payment.getStatus())) {
            throw new BusinessException(HttpStatus.CONFLICT, "DUPLICATE_REFUND", "Giao dịch này đã được hoàn tiền toàn bộ trước đó");
        }
        if (!"PAID".equals(payment.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "PAYMENT_NOT_PAID", "Chỉ có thể hoàn tiền cho giao dịch đã thanh toán thành công");
        }

        // 2. Tính tổng tiền đã hoàn / đang chờ hoàn trên giao dịch này
        List<Refund> existingRefunds = refundRepository.findAllByPaymentIdOrderByCreatedAtAsc(payment.getId());
        BigDecimal alreadyRefunded = existingRefunds.stream()
            .filter(r -> "COMPLETED".equals(r.getStatus()) || "PENDING".equals(r.getStatus()))
            .map(Refund::getAmount)
            .reduce(ZERO_MONEY, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

        BigDecimal remainingRefundable = payment.getAmount().subtract(alreadyRefunded).setScale(2, RoundingMode.HALF_UP);
        if (remainingRefundable.compareTo(ZERO_MONEY) <= 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "DUPLICATE_REFUND", "Không còn số dư khả dụng để hoàn tiền");
        }

        BigDecimal refundAmount = (request.amount() != null)
            ? request.amount().setScale(2, RoundingMode.HALF_UP)
            : remainingRefundable;

        if (refundAmount.compareTo(remainingRefundable) > 0) {
            throw new BusinessException(
                HttpStatus.CONFLICT,
                "REFUND_EXCEEDS_PAID_AMOUNT",
                "Số tiền hoàn (" + refundAmount + ") vượt quá số dư có thể hoàn (" + remainingRefundable + ")"
            );
        }

        OffsetDateTime now = OffsetDateTime.now();
        Refund refund = Refund.builder()
            .paymentId(payment.getId())
            .complaintId(request.complaintId())
            .amount(refundAmount)
            .reason(request.reason().trim())
            .status("COMPLETED")
            .createdAt(now)
            .processedAt(now)
            .build();

        Refund savedRefund = refundRepository.save(refund);

        // Nếu tổng tiền hoàn khớp 100% số tiền giao dịch gốc -> đánh dấu REFUNDED
        if (alreadyRefunded.add(refundAmount).compareTo(payment.getAmount()) == 0) {
            payment.setStatus("REFUNDED");
            paymentRepository.save(payment);
        }

        recordAuditLog(adminId, "PROCESS_REFUND", "refunds", savedRefund.getId());
        return RefundResponse.from(savedRefund);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getOrderPayments(Long actorId, String role, Long orderId) {
        Order order = orderRepository.findWithDetailsById(orderId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Không tìm thấy đơn hàng"));

        boolean isAllowed = "ADMIN".equals(role)
            || ("BUYER".equals(role) && order.getBuyerId().equals(actorId))
            || ("FARMER".equals(role) && orderRepository.existsOrderForFarmer(orderId, actorId));

        if (!isAllowed) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "ORDER_ACCESS_DENIED", "Bạn không có quyền xem giao dịch của đơn hàng này");
        }

        BigDecimal orderTotal = calculateExactOrderTotal(order);
        return paymentRepository.findAllByOrderIdOrderByCreatedAtAsc(orderId).stream()
            .map(p -> PaymentResponse.from(p, orderTotal))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderFinancialReconciliationResponse reconcileOrderFinances(Long orderId) {
        Order order = orderRepository.findWithDetailsById(orderId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Không tìm thấy đơn hàng"));

        BigDecimal orderTotal = calculateExactOrderTotal(order);
        List<Payment> payments = paymentRepository.findAllByOrderIdOrderByCreatedAtAsc(orderId);

        BigDecimal totalPaid = payments.stream()
            .filter(p -> "PAID".equals(p.getStatus()) || "REFUNDED".equals(p.getStatus()))
            .map(Payment::getAmount)
            .reduce(ZERO_MONEY, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

        Set<Long> paymentIds = payments.stream().map(Payment::getId).collect(Collectors.toSet());
        List<Refund> refunds = paymentIds.isEmpty()
            ? List.of()
            : refundRepository.findAllByPaymentIdInOrderByCreatedAtAsc(paymentIds);

        BigDecimal totalRefunded = refunds.stream()
            .filter(r -> "COMPLETED".equals(r.getStatus()))
            .map(Refund::getAmount)
            .reduce(ZERO_MONEY, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

        BigDecimal netCollected = totalPaid.subtract(totalRefunded).setScale(2, RoundingMode.HALF_UP);

        // Khớp 100% khi không thu vượt quá orderTotal và không hoàn vượt quá totalPaid
        boolean exactMatch = totalPaid.compareTo(orderTotal) <= 0
            && totalRefunded.compareTo(totalPaid) <= 0
            && (totalPaid.compareTo(ZERO_MONEY) == 0 || totalPaid.compareTo(orderTotal) == 0 || hasOnlyDeposit(payments));

        return new OrderFinancialReconciliationResponse(
            order.getId(),
            order.getStatus(),
            order.getPaymentMethod(),
            orderTotal,
            totalPaid,
            totalRefunded,
            netCollected,
            exactMatch,
            payments.stream().map(p -> PaymentResponse.from(p, orderTotal)).toList(),
            refunds.stream().map(RefundResponse::from).toList()
        );
    }

    private BigDecimal resolveExactChargeAmount(
        String paymentType,
        BigDecimal orderTotal,
        BigDecimal totalAlreadySettled,
        List<Payment> settledPayments,
        BigDecimal requestedDepositRate
    ) {
        return switch (paymentType) {
            case "FULL" -> {
                if (!settledPayments.isEmpty()) {
                    throw new BusinessException(
                        HttpStatus.CONFLICT,
                        "DUPLICATE_PAYMENT",
                        "Đơn hàng đã phát sinh giao dịch thanh toán, không thể thanh toán toàn bộ trùng lặp"
                    );
                }
                yield orderTotal;
            }
            case "DEPOSIT" -> {
                if (!settledPayments.isEmpty()) {
                    throw new BusinessException(
                        HttpStatus.CONFLICT,
                        "DUPLICATE_PAYMENT",
                        "Đơn hàng đã được đặt cọc hoặc thanh toán trước đó"
                    );
                }
                BigDecimal rate = (requestedDepositRate != null) ? requestedDepositRate : DEFAULT_DEPOSIT_RATE;
                yield orderTotal.multiply(rate).setScale(2, RoundingMode.HALF_UP);
            }
            case "REMAINING" -> {
                boolean hasPaidDeposit = settledPayments.stream().anyMatch(p -> "DEPOSIT".equals(p.getPaymentType()));
                boolean hasOtherPayment = settledPayments.stream().anyMatch(p -> !"DEPOSIT".equals(p.getPaymentType()));

                if (!hasPaidDeposit) {
                    throw new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "DEPOSIT_REQUIRED",
                        "Đơn hàng chưa hoàn tất đặt cọc nên chưa thể thanh toán phần còn lại"
                    );
                }
                if (hasOtherPayment || totalAlreadySettled.compareTo(orderTotal) >= 0) {
                    throw new BusinessException(
                        HttpStatus.CONFLICT,
                        "DUPLICATE_PAYMENT",
                        "Phần còn lại của đơn hàng đã được thanh toán đủ"
                    );
                }
                // Phần còn lại = Tổng đơn hàng - Tiền đã cọc => Tổng luôn khớp 100% tới từng xu
                yield orderTotal.subtract(totalAlreadySettled).setScale(2, RoundingMode.HALF_UP);
            }
            default -> throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_PAYMENT_TYPE", "Loại thanh toán không hợp lệ");
        };
    }

    private BigDecimal calculateExactOrderTotal(Order order) {
        BigDecimal itemsSubtotal = ZERO_MONEY;
        for (OrderItem item : order.getItems()) {
            BigDecimal lineTotal = item.getUnitPrice()
                .multiply(item.getQuantity())
                .setScale(2, RoundingMode.HALF_UP);
            itemsSubtotal = itemsSubtotal.add(lineTotal);
        }
        return itemsSubtotal.add(order.getShippingFee()).setScale(2, RoundingMode.HALF_UP);
    }

    private boolean hasOnlyDeposit(List<Payment> payments) {
        List<Payment> active = payments.stream()
            .filter(p -> "PAID".equals(p.getStatus()) || "REFUNDED".equals(p.getStatus()))
            .toList();
        return active.size() == 1 && "DEPOSIT".equals(active.get(0).getPaymentType());
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
