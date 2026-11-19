package com.nsv.backend.order;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.order.dto.*;
import com.nsv.backend.order.entity.Order;
import com.nsv.backend.order.entity.OrderItem;
import com.nsv.backend.order.entity.Payment;
import com.nsv.backend.order.entity.Refund;
import com.nsv.backend.order.repository.OrderRepository;
import com.nsv.backend.order.repository.PaymentRepository;
import com.nsv.backend.order.repository.RefundRepository;
import com.nsv.backend.order.service.FinancialTransactionServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TcFin01Test {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private FinancialTransactionServiceImpl financialService;

    private Order buildOrderWithOddAmount(Long orderId, Long buyerId) {
        // 3 * 33,333.33 = 99,999.99 + 15,000.01 ship = 115,000.00
        // hoặc 1 * 133,333.00 + 0 = 133,333.00 (chia 30% lẻ xu để kiểm tra làm tròn)
        Order order = Order.builder()
            .id(orderId)
            .buyerId(buyerId)
            .paymentMethod("ONLINE")
            .status("PENDING")
            .shippingFee(new BigDecimal("15000.00"))
            .build();

        order.addItem(OrderItem.builder()
            .id(1L)
            .batchId(101L)
            .quantity(new BigDecimal("3.00"))
            .unitPrice(new BigDecimal("39444.33")) // 3 * 39444.33 = 118,332.99
            .build());
        // Tổng đơn = 118,332.99 + 15,000.00 = 133,332.99
        return order;
    }

    @Test
    @DisplayName("TC-FIN-01 (1a): Thanh toán FULL khớp 100% tổng giá trị đơn hàng")
    void processFullPayment_ExactMatchWithOrderTotal() {
        Long orderId = 500L;
        Long buyerId = 20L;
        Order order = buildOrderWithOddAmount(orderId, buyerId);
        BigDecimal expectedOrderTotal = new BigDecimal("133332.99");

        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));
        when(paymentRepository.findAllByOrderIdOrderByCreatedAtAsc(orderId)).thenReturn(List.of());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        PaymentResponse response = financialService.processPayment(
            buyerId, false, new ProcessPaymentRequest(orderId, "FULL", null, null)
        );

        assertEquals(expectedOrderTotal, response.amount());
        assertEquals(expectedOrderTotal, response.orderTotal());
        assertEquals("PAID", response.status());
    }

    @Test
    @DisplayName("TC-FIN-01 (1b): Đặt cọc DEPOSIT (30%) + REMAINING (70%) trên số lẻ khớp 100% tổng đơn hàng")
    void processDepositAndRemaining_OddOrderTotal_SumsToExact100Percent() {
        Long orderId = 501L;
        Long buyerId = 20L;
        Order order = buildOrderWithOddAmount(orderId, buyerId);
        BigDecimal expectedOrderTotal = new BigDecimal("133332.99");
        List<Payment> storedPayments = new ArrayList<>();

        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.findWithDetailsById(orderId)).thenReturn(Optional.of(order));
        when(paymentRepository.findAllByOrderIdOrderByCreatedAtAsc(orderId)).thenAnswer(inv -> List.copyOf(storedPayments));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId((long) (storedPayments.size() + 1));
            storedPayments.add(p);
            return p;
        });

        // Bước 1: Thanh toán đặt cọc 30% -> 133332.99 * 0.30 = 39999.897 -> 39999.90
        PaymentResponse deposit = financialService.processPayment(
            buyerId, false, new ProcessPaymentRequest(orderId, "DEPOSIT", new BigDecimal("0.30"), null)
        );
        assertEquals(new BigDecimal("39999.90"), deposit.amount());

        // Bước 2: Thanh toán phần còn lại -> 133332.99 - 39999.90 = 93333.09
        PaymentResponse remaining = financialService.processPayment(
            buyerId, false, new ProcessPaymentRequest(orderId, "REMAINING", null, null)
        );
        assertEquals(new BigDecimal("93333.09"), remaining.amount());

        // Bước 3: Đối soát Admin -> Tổng thu khớp 100.00% tổng đơn hàng
        OrderFinancialReconciliationResponse recon = financialService.reconcileOrderFinances(orderId);
        assertEquals(expectedOrderTotal, recon.totalPaid());
        assertEquals(expectedOrderTotal, recon.netCollected());
        assertTrue(recon.exactAmountMatch());
    }

    @Test
    @DisplayName("TC-FIN-01 (2a): Concurrency 10 luồng thanh toán đồng thời cùng 1 đơn -> chỉ 1 giao dịch thành công, không trừ trùng")
    void concurrentPayments_SameOrder_OnlyOneSucceedsNoDuplicateCharge() throws Exception {
        Long orderId = 502L;
        Long buyerId = 20L;
        Order order = buildOrderWithOddAmount(orderId, buyerId);
        BigDecimal expectedOrderTotal = new BigDecimal("133332.99");

        ReentrantLock dbRowLock = new ReentrantLock();
        List<Payment> threadSafePayments = Collections.synchronizedList(new ArrayList<>());
        AtomicLong idGenerator = new AtomicLong(1);

        // Mô phỏng khóa hàng SELECT ... FOR UPDATE của PostgreSQL
        when(orderRepository.findByIdForUpdate(orderId)).thenAnswer(inv -> {
            dbRowLock.lock();
            return Optional.of(order);
        });
        when(paymentRepository.findAllByOrderIdOrderByCreatedAtAsc(orderId))
            .thenAnswer(inv -> new ArrayList<>(threadSafePayments));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(idGenerator.getAndIncrement());
            threadSafePayments.add(p);
            return p;
        });

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CyclicBarrier startBarrier = new CyclicBarrier(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger duplicateRejectedCount = new AtomicInteger(0);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                try {
                    startBarrier.await(5, TimeUnit.SECONDS);
                    try {
                        financialService.processPayment(
                            buyerId, false, new ProcessPaymentRequest(orderId, "FULL", null, null)
                        );
                        successCount.incrementAndGet();
                    } finally {
                        if (dbRowLock.isHeldByCurrentThread()) {
                            dbRowLock.unlock();
                        }
                    }
                } catch (BusinessException ex) {
                    if ("DUPLICATE_PAYMENT".equals(ex.getCode())) {
                        duplicateRejectedCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }

        for (Future<?> future : futures) {
            future.get(10, TimeUnit.SECONDS);
        }
        executor.shutdown();

        assertEquals(1, successCount.get(), "Chỉ được phép có duy nhất 1 giao dịch thành công");
        assertEquals(threadCount - 1, duplicateRejectedCount.get(), "9 luồng còn lại phải bị chặn trùng lặp");
        assertEquals(1, threadSafePayments.size());

        BigDecimal totalRecorded = threadSafePayments.stream()
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), BigDecimal::add);
        assertEquals(expectedOrderTotal, totalRecorded, "Tổng tiền ghi nhận phải khớp 100% số tiền đơn hàng");
    }

    @Test
    @DisplayName("TC-FIN-01 (2b): Concurrency 10 luồng hoàn tiền đồng thời cùng 1 giao dịch -> chỉ 1 hoàn tiền thành công, không cộng trùng")
    void concurrentRefunds_SamePayment_OnlyOneSucceedsNoDuplicateRefund() throws Exception {
        Long paymentId = 700L;
        Long adminId = 1L;
        BigDecimal paidAmount = new BigDecimal("133332.99");

        Payment payment = Payment.builder()
            .id(paymentId)
            .orderId(502L)
            .paymentType("FULL")
            .amount(paidAmount)
            .status("PAID")
            .build();

        ReentrantLock dbRowLock = new ReentrantLock();
        List<Refund> threadSafeRefunds = Collections.synchronizedList(new ArrayList<>());
        AtomicLong idGenerator = new AtomicLong(1);

        when(paymentRepository.findByIdForUpdate(paymentId)).thenAnswer(inv -> {
            dbRowLock.lock();
            return Optional.of(payment);
        });
        when(refundRepository.findAllByPaymentIdOrderByCreatedAtAsc(paymentId))
            .thenAnswer(inv -> new ArrayList<>(threadSafeRefunds));
        when(refundRepository.save(any(Refund.class))).thenAnswer(inv -> {
            Refund r = inv.getArgument(0);
            r.setId(idGenerator.getAndIncrement());
            threadSafeRefunds.add(r);
            return r;
        });

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CyclicBarrier startBarrier = new CyclicBarrier(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger duplicateRejectedCount = new AtomicInteger(0);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                try {
                    startBarrier.await(5, TimeUnit.SECONDS);
                    try {
                        financialService.processRefund(
                            adminId, new ProcessRefundRequest(paymentId, 12L, null, "Hoàn tiền do khiếu nại hợp lệ")
                        );
                        successCount.incrementAndGet();
                    } finally {
                        if (dbRowLock.isHeldByCurrentThread()) {
                            dbRowLock.unlock();
                        }
                    }
                } catch (BusinessException ex) {
                    if ("DUPLICATE_REFUND".equals(ex.getCode())) {
                        duplicateRejectedCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }

        for (Future<?> future : futures) {
            future.get(10, TimeUnit.SECONDS);
        }
        executor.shutdown();

        assertEquals(1, successCount.get(), "Chỉ được phép có duy nhất 1 giao dịch hoàn tiền thành công");
        assertEquals(threadCount - 1, duplicateRejectedCount.get(), "9 luồng hoàn tiền đồng thời còn lại phải bị chặn");
        assertEquals("REFUNDED", payment.getStatus());

        BigDecimal totalRefunded = threadSafeRefunds.stream()
            .map(Refund::getAmount)
            .reduce(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), BigDecimal::add);
        assertEquals(paidAmount, totalRefunded, "Tổng tiền hoàn phải khớp 100% số tiền đã thanh toán");
    }
}
