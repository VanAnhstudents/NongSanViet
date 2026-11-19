package com.nsv.backend.dispute;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.dispute.dto.CreateComplaintRequest;
import com.nsv.backend.dispute.dto.DisputeDetailResponse;
import com.nsv.backend.dispute.dto.EvidenceItemRequest;
import com.nsv.backend.dispute.entity.Complaint;
import com.nsv.backend.dispute.repository.ComplaintRepository;
import com.nsv.backend.dispute.service.DisputeServiceImpl;
import com.nsv.backend.inventory.repository.BatchRepository;
import com.nsv.backend.order.entity.Order;
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
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TC-COMPLAIN-01 — PBI-29: Buyer gửi khiếu nại kèm tối thiểu 1 ảnh/video trong vòng 24h
 * kể từ khi đơn hàng "Đã giao"; hệ thống từ chối khiếu nại gửi sau hạn 24h.
 *
 * <p>Lưu ý: test này phụ thuộc DisputeServiceImpl biên dịch được — hiện service đang lỗi
 * (goi RefundRepository.findAllByPaymentIdInOrderByCreatedAtAsc chưa có); sau khi sửa main
 * (bổ sung method cho RefundRepository) thì test sẽ chạy. Test (5) phản ánh yêu cầu hạn 24h
 * chưa được hiện thực -> sẽ fail cho đến khi bổ sung kiểm tra thời gian ở createComplaint.</p>
 */
@ExtendWith(MockitoExtension.class)
class TcComplain01Test {

    private static final Long BUYER_ID = 20L;
    private static final Long ORDER_ID = 501L;

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

    private Order order(String status, OffsetDateTime deliveredAt) {
        return Order.builder()
                .id(ORDER_ID).buyerId(BUYER_ID).status(status).shippingFee(BigDecimal.ZERO)
                .deliveredAt(deliveredAt).build();
    }

    private CreateComplaintRequest oneEvidenceRequest() {
        return new CreateComplaintRequest(
                "Sản phẩm bị dập nát",
                "Hơn 40% số lượng vải bị dập khi mở hộp",
                List.of(new EvidenceItemRequest("IMAGE", "https://cdn.nongsanviet.vn/complaint/a1.jpg", "Ảnh vải dập"))
        );
    }

    @Test
    @DisplayName("TC-COMPLAIN-01 (1a): Buyer gửi khiếu nại kèm 1 ảnh cho đơn Đã giao -> tạo thành công, lưu bằng chứng")
    void createComplaint_deliveredOrderWithEvidence_succeeds() {
        when(orderRepository.findWithDetailsById(ORDER_ID)).thenReturn(Optional.of(order("DELIVERED", null)));
        when(complaintRepository.existsByOrderId(ORDER_ID)).thenReturn(false);
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(inv -> {
            Complaint c = inv.getArgument(0);
            c.setId(301L);
            return c;
        });
        when(orderRepository.findDistinctFarmerIdsByOrderId(ORDER_ID)).thenReturn(List.of(10L));

        DisputeDetailResponse response = disputeService.createComplaint(BUYER_ID, ORDER_ID, oneEvidenceRequest());

        assertEquals(301L, response.complaintId());
        assertEquals(ORDER_ID, response.orderId());
        assertEquals("OPEN", response.complaintStatus());
        assertEquals(1, response.evidences().size());
        assertEquals("IMAGE", response.evidences().get(0).mediaType());
        assertEquals("BUYER", response.evidences().get(0).uploaderRole());
        assertEquals(BUYER_ID, response.evidences().get(0).uploadedBy());
        verify(complaintRepository).save(any(Complaint.class));
    }

    @Test
    @DisplayName("TC-COMPLAIN-01 (1b): Đơn PENDING/CANCELLED không thể tạo khiếu nại (400)")
    void createComplaint_pendingOrder_rejected() {
        when(orderRepository.findWithDetailsById(ORDER_ID)).thenReturn(Optional.of(order("PENDING", null)));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> disputeService.createComplaint(BUYER_ID, ORDER_ID, oneEvidenceRequest()));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("INVALID_ORDER_STATUS_FOR_COMPLAINT", ex.getCode());
        verify(complaintRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-COMPLAIN-01 (1c): Người mua khác không thể khiếu nại đơn không thuộc mình (403)")
    void createComplaint_notOrderOwner_rejected() {
        when(orderRepository.findWithDetailsById(ORDER_ID))
                .thenReturn(Optional.of(Order.builder().id(ORDER_ID).buyerId(999L).status("DELIVERED").build()));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> disputeService.createComplaint(BUYER_ID, ORDER_ID, oneEvidenceRequest()));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("ORDER_ACCESS_DENIED", ex.getCode());
    }

    @Test
    @DisplayName("TC-COMPLAIN-01 (1d): Đơn đã có khiếu nại -> từ chối trùng lặp (409)")
    void createComplaint_duplicateComplaint_rejected() {
        when(orderRepository.findWithDetailsById(ORDER_ID)).thenReturn(Optional.of(order("DELIVERED", null)));
        when(complaintRepository.existsByOrderId(ORDER_ID)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> disputeService.createComplaint(BUYER_ID, ORDER_ID, oneEvidenceRequest()));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("COMPLAINT_ALREADY_EXISTS", ex.getCode());
        verify(complaintRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-COMPLAIN-01 (2): Khiếu nại gửi sau 24h kể từ 'Đã giao' -> bị từ chối (chưa hiện thực -> FAIL)")
    void createComplaint_after24hWindow_rejected() {
        OffsetDateTime delivered25hAgo = OffsetDateTime.now().minusHours(25);
        when(orderRepository.findWithDetailsById(ORDER_ID)).thenReturn(Optional.of(order("DELIVERED", delivered25hAgo)));
        when(complaintRepository.existsByOrderId(ORDER_ID)).thenReturn(false);
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(inv -> {
            Complaint c = inv.getArgument(0);
            c.setId(302L);
            return c;
        });
        when(orderRepository.findDistinctFarmerIdsByOrderId(ORDER_ID)).thenReturn(List.of(10L));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> disputeService.createComplaint(BUYER_ID, ORDER_ID, oneEvidenceRequest()));

        // Hệ thống phải từ chối khiếu nại quá hạn 24h với mã rõ ràng.
        assertEquals("COMPLAINT_WINDOW_EXPIRED", ex.getCode());
    }
}
