package com.nsv.backend.dispute;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.dispute.dto.DisputeDetailResponse;
import com.nsv.backend.dispute.dto.ResolveDisputeRequest;
import com.nsv.backend.dispute.entity.Complaint;
import com.nsv.backend.dispute.entity.ComplaintEvidence;
import com.nsv.backend.dispute.repository.ComplaintRepository;
import com.nsv.backend.dispute.service.DisputeServiceImpl;
import com.nsv.backend.inventory.repository.BatchRepository;
import com.nsv.backend.order.dto.ProcessRefundRequest;
import com.nsv.backend.order.dto.RefundResponse;
import com.nsv.backend.order.entity.Order;
import com.nsv.backend.order.entity.Payment;
import com.nsv.backend.order.repository.OrderCoInspectionRepository;
import com.nsv.backend.order.repository.OrderRepository;
import com.nsv.backend.order.repository.PaymentRepository;
import com.nsv.backend.order.repository.RefundRepository;
import com.nsv.backend.order.service.FinancialTransactionService;
import com.nsv.backend.product.repository.ProductRepository;
import com.nsv.backend.trace.repository.FarmingLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * TC-DISPUTE-01 — PBI-31: Admin xem bằng chứng và phân xử tranh chấp; khi quyết định hoàn tiền,
 * giao dịch hoàn tiền được thực hiện chính xác và cập nhật trạng thái đơn/khiếu nại.
 *
 * <p>Phụ thuộc DisputeServiceImpl biên dịch được — service đang lỗi do gọi
 * RefundRepository.findAllByPaymentIdInOrderByCreatedAtAsc (chưa có trên RefundRepository).
 * Sau khi bổ sung method đó cho RefundRepository, test sẽ chạy.</p>
 */
@ExtendWith(MockitoExtension.class)
class TcDispute01Test {

    private static final Long ADMIN_ID = 1L;
    private static final Long COMPLAINT_ID = 301L;
    private static final Long ORDER_ID = 501L;
    private static final Long BUYER_ID = 20L;

    @Mock
    private ComplaintRepository complaintRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private RefundRepository refundRepository;
    @Mock
    private BatchRepository batchRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private FarmingLogRepository farmingLogRepository;
    @Mock
    private OrderCoInspectionRepository coInspectionRepository;
    @Mock
    private FinancialTransactionService financialTransactionService;
    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private DisputeServiceImpl disputeService;

    private Complaint complaintWithEvidence(String status) {
        Complaint complaint = Complaint.builder()
                .id(COMPLAINT_ID).orderId(ORDER_ID).buyerId(BUYER_ID)
                .reason("Sản phẩm hỏng").description("Mô tả").status(status).build();
        complaint.addEvidence(ComplaintEvidence.builder()
                .uploadedBy(BUYER_ID).uploaderRole("BUYER")
                .mediaType("IMAGE").mediaUrl("https://cdn.nongsanviet.vn/c/a1.jpg")
                .caption("Ảnh hàng hỏng").build());
        return complaint;
    }

    private Order order(String status) {
        return Order.builder().id(ORDER_ID).buyerId(BUYER_ID).status(status)
                .shippingFee(BigDecimal.ZERO).build();
    }

    @Test
    @DisplayName("TC-DISPUTE-01 (1a): Admin xem bằng chứng và REJECT_COMPLAINT -> RESOLVED_REJECTED, đơn DELIVERED -> COMPLETED")
    void rejectComplaint_resolvesAsRejectedAndCompletesOrder() {
        Complaint complaint = complaintWithEvidence("IN_REVIEW");
        Order order = order("DELIVERED");

        when(complaintRepository.findByIdForUpdate(COMPLAINT_ID)).thenReturn(Optional.of(complaint));
        when(orderRepository.findByIdForUpdate(ORDER_ID)).thenReturn(Optional.of(order));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(complaintRepository.findWithEvidencesById(COMPLAINT_ID)).thenReturn(Optional.of(complaint));
        when(orderRepository.findWithDetailsById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.findDistinctFarmerIdsByOrderId(ORDER_ID)).thenReturn(List.of(10L));

        DisputeDetailResponse response = disputeService.resolveDisputeByAdmin(ADMIN_ID, COMPLAINT_ID,
                new ResolveDisputeRequest("REJECT_COMPLAINT", "Bằng chứng không đủ, giữ nguyên thanh toán", null));

        assertEquals("RESOLVED_REJECTED", response.complaintStatus());
        assertEquals("REJECT_COMPLAINT", response.adminDecision());
        assertEquals(0, response.refundAmount().compareTo(BigDecimal.ZERO));
        assertEquals("COMPLETED", response.orderStatus());
        assertEquals(1, response.evidences().size());
        assertEquals("IMAGE", response.evidences().get(0).mediaType());
    }

    @Test
    @DisplayName("TC-DISPUTE-01 (1b): Tranh chấp đã phân xử -> từ chối phân xử lại (409)")
    void alreadyResolved_rejectsWithConflict() {
        Complaint complaint = Complaint.builder()
                .id(COMPLAINT_ID).orderId(ORDER_ID).buyerId(BUYER_ID).status("RESOLVED_REFUND").build();
        when(complaintRepository.findByIdForUpdate(COMPLAINT_ID)).thenReturn(Optional.of(complaint));

        BusinessException ex = assertThrows(BusinessException.class, () -> disputeService.resolveDisputeByAdmin(
                ADMIN_ID, COMPLAINT_ID,
                new ResolveDisputeRequest("REJECT_COMPLAINT", "Xem lại", null)));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("DISPUTE_ALREADY_RESOLVED", ex.getCode());
        verify(financialTransactionService, never()).processRefund(anyLong(), any());
    }

    @Test
    @DisplayName("TC-DISPUTE-01 (2): FULL_REFUND hoàn đúng 100% tiền đã thanh toán, đơn chuyển CANCELLED")
    void fullRefund_refundsExactPaidAmountAndCancelsOrder() {
        Complaint complaint = complaintWithEvidence("IN_REVIEW");
        Order order = order("DELIVERED");
        Payment paidPayment = Payment.builder()
                .id(1L).orderId(ORDER_ID).paymentType("FULL")
                .amount(new BigDecimal("100000.00")).status("PAID").providerRef("PAY-501").build();

        when(complaintRepository.findByIdForUpdate(COMPLAINT_ID)).thenReturn(Optional.of(complaint));
        when(orderRepository.findByIdForUpdate(ORDER_ID)).thenReturn(Optional.of(order));
        when(paymentRepository.findAllByOrderIdOrderByCreatedAtAsc(ORDER_ID)).thenReturn(List.of(paidPayment));
        when(refundRepository.findAllByPaymentIdInOrderByCreatedAtAsc(anySet())).thenReturn(List.of());
        when(financialTransactionService.processRefund(eq(ADMIN_ID), any(ProcessRefundRequest.class)))
                .thenReturn(new RefundResponse(900L, 1L, COMPLAINT_ID, new BigDecimal("100000.00"),
                        "Admin phân xử hoàn toàn", "COMPLETED", null, null));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(complaintRepository.findWithEvidencesById(COMPLAINT_ID)).thenReturn(Optional.of(complaint));
        when(orderRepository.findWithDetailsById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.findDistinctFarmerIdsByOrderId(ORDER_ID)).thenReturn(List.of(10L));

        DisputeDetailResponse response = disputeService.resolveDisputeByAdmin(ADMIN_ID, COMPLAINT_ID,
                new ResolveDisputeRequest("FULL_REFUND", "Chấp nhận hoàn tiền do lỗi của người bán", null));

        assertEquals("RESOLVED_REFUND", response.complaintStatus());
        assertEquals("FULL_REFUND", response.adminDecision());
        assertEquals(0, response.refundAmount().compareTo(new BigDecimal("100000.00")));
        assertEquals("CANCELLED", response.orderStatus());
        verify(financialTransactionService, times(1)).processRefund(eq(ADMIN_ID), any(ProcessRefundRequest.class));
    }
}
