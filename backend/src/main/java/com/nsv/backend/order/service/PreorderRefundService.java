package com.nsv.backend.order.service;

import com.nsv.backend.common.exception.ApiException;
import com.nsv.backend.order.dto.RefundItem;
import com.nsv.backend.order.dto.ShortageRefundResponse;
import com.nsv.backend.order.entity.Order;
import com.nsv.backend.order.entity.Payment;
import com.nsv.backend.order.entity.Refund;
import com.nsv.backend.order.repository.OrderRepository;
import com.nsv.backend.order.repository.RefundPaymentRepository;
import com.nsv.backend.order.repository.RefundQueryRepository;
import com.nsv.backend.order.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Clock;
<<<<<<< HEAD
import java.time.Instant;
import java.time.ZoneOffset;
=======
import java.time.OffsetDateTime;
>>>>>>> origin/develop
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * PBI-32: Farmer báo thiếu hàng cho đơn Pre-order -> hệ thống tự hoàn 100% các khoản đã thanh toán (BR-05),
 * hủy đơn (orders.status = CANCELLED) và thông báo cho Buyer.
 */
@Service
@RequiredArgsConstructor
public class PreorderRefundService {

<<<<<<< HEAD
    private static final Set<String> CANCELLABLE = Set.of(OrderStatus.PENDING.name(), OrderStatus.CONFIRMED.name());
=======
    private static final Set<String> CANCELLABLE = Set.of("PENDING", "CONFIRMED");
>>>>>>> origin/develop
    private static final String REASON_PREFIX = "Người bán báo thiếu hàng Pre-order: ";

    private final OrderRepository orderRepository;
    private final RefundPaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final RefundQueryRepository refundQueryRepository;
    private final BuyerNotifier buyerNotifier;
    private final Clock clock;

    @Transactional
    public ShortageRefundResponse reportShortage(Long farmerId, Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .filter(o -> refundQueryRepository.orderBelongsToFarmer(orderId, farmerId))
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy đơn hàng của bạn"));
        if (refundQueryRepository.orderHasOtherFarmers(orderId, farmerId)) {
            throw invalidState("Đơn hàng có sản phẩm của nhiều người bán, chưa hỗ trợ hoàn tiền tự động");
        }
        if (!refundQueryRepository.isPreOrder(orderId)) {
            throw invalidState("Chỉ báo thiếu hàng được với đơn Pre-order");
        }
        if (!CANCELLABLE.contains(order.getStatus())) {
            throw invalidState("Đơn hàng không còn ở trạng thái có thể hủy do thiếu hàng");
        }

        OffsetDateTime now = OffsetDateTime.now(clock);
        String refundReason = limit(REASON_PREFIX + reason.trim(), 255);
        List<RefundItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Payment payment : paymentRepository.findByOrderIdAndStatus(orderId, "PAID")) {
            Refund refund = refundRepository.save(Refund.builder()
                    .paymentId(payment.getId())
                    .amount(payment.getAmount())
                    .reason(refundReason)
                    .status("COMPLETED")
                    .createdAt(now)
                    .processedAt(now)
                    .build());
<<<<<<< HEAD
            payment.setStatus(PaymentStatus.REFUNDED.name());
=======
            payment.setStatus("REFUNDED");
>>>>>>> origin/develop
            paymentRepository.save(payment);
            items.add(new RefundItem(refund.getId(), payment.getId(), refund.getAmount(), refund.getStatus()));
            total = total.add(payment.getAmount());
        }

<<<<<<< HEAD
        order.setStatus(OrderStatus.CANCELLED.name());
        order.setUpdatedAt(now.atOffset(ZoneOffset.UTC));
=======
        order.setStatus("CANCELLED");
        order.setUpdatedAt(now);
>>>>>>> origin/develop
        orderRepository.save(order);

        buyerNotifier.notifyBuyer(order.getBuyerId(), message(orderId, total));
        return new ShortageRefundResponse(orderId, "CANCELLED", total, items);
    }

    private static String message(Long orderId, BigDecimal total) {
        String base = "Đơn hàng #" + orderId + " đã bị hủy vì người bán không đủ hàng Pre-order.";
        if (total.signum() == 0) {
            return base + " Đơn chưa phát sinh khoản thanh toán nên không có tiền cần hoàn.";
        }
        String amount = NumberFormat.getInstance(Locale.of("vi", "VN")).format(total);
        return base + " Số tiền " + amount + "đ đã được hoàn lại cho bạn.";
    }

    private static String limit(String text, int max) {
        return text.length() > max ? text.substring(0, max) : text;
    }

    private static ApiException invalidState(String message) {
        return new ApiException(HttpStatus.CONFLICT, "INVALID_STATE", message, null);
    }
}
