package com.nsv.backend.dispute;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.dispute.dto.*;
import com.nsv.backend.dispute.entity.Complaint;
import com.nsv.backend.dispute.entity.ComplaintEvidence;
import com.nsv.backend.dispute.repository.ComplaintRepository;
import com.nsv.backend.dispute.service.DisputeServiceImpl;
import com.nsv.backend.dispute.storage.EvidenceStorageService;
import com.nsv.backend.dispute.storage.StoredEvidenceFile;
import com.nsv.backend.inventory.repository.BatchRepository;
import com.nsv.backend.order.entity.Order;
import com.nsv.backend.order.entity.OrderItem;
import com.nsv.backend.order.repository.OrderCoInspectionRepository;
import com.nsv.backend.order.repository.OrderRepository;
import com.nsv.backend.order.repository.PaymentRepository;
import com.nsv.backend.order.repository.RefundRepository;
import com.nsv.backend.order.service.FinancialTransactionService;
import com.nsv.backend.product.repository.ProductRepository;
import com.nsv.backend.trace.repository.FarmingLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuyerComplaintSlaTest {

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
    private EvidenceStorageService evidenceStorageService;

    @Mock
    private JdbcTemplate jdbcTemplate;

    private DisputeServiceImpl disputeService;

    @BeforeEach
    void setUp() {
        disputeService = new DisputeServiceImpl(
            complaintRepository,
            orderRepository,
            paymentRepository,
            refundRepository,
            batchRepository,
            productRepository,
            farmingLogRepository,
            coInspectionRepository,
            financialTransactionService,
            evidenceStorageService,
            jdbcTemplate
        );
    }

    private Order buildDeliveredOrder(Long orderId, Long buyerId, OffsetDateTime deliveredAt) {
        Order order = Order.builder()
            .id(orderId)
            .buyerId(buyerId)
            .paymentMethod("ONLINE")
            .status("DELIVERED")
            .shippingFee(new BigDecimal("15000.00"))
            .deliveredAt(deliveredAt)
            .build();

        order.addItem(OrderItem.builder()
            .id(1L)
            .batchId(101L)
            .quantity(new BigDecimal("2.00"))
            .unitPrice(new BigDecimal("45000.00"))
            .build());
        return order;
    }

    @SuppressWarnings("unchecked")
    private void mockDetailDependencies(Long orderId) {
        when(batchRepository.findAllByIdIn(Set.of(101L))).thenReturn(List.of());
        when(farmingLogRepository.findAllByBatchIdOrderByLogDateAscIdAsc(101L)).thenReturn(List.of());
        when(coInspectionRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(paymentRepository.findAllByOrderIdOrderByCreatedAtAsc(orderId)).thenReturn(List.of());
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(orderId), anyLong())).thenReturn(List.of());
    }

    @Test
    @DisplayName("TC-COMPLAIN-01 (1a): Buyer gửi khiếu nại kèm ảnh/video trong vòng 24h sau nhận hàng -> thành công")
    void createComplaint_Within24HoursWithEvidences_Succeeds() {
        Long buyerId = 20L;
        Long orderId = 501L;
        Long farmerId = 10L;

        OffsetDateTime deliveredAt = OffsetDateTime.of(2026, 6, 15, 10, 0, 0, 0, ZoneOffset.ofHours(7));
        // Gửi khiếu nại sau khi giao 3 giờ 30 phút (nằm trong SLA 24h)
        OffsetDateTime submitTime = deliveredAt.plusHours(3).plusMinutes(30);
        disputeService.setClock(Clock.fixed(submitTime.toInstant(), ZoneOffset.ofHours(7)));

        Order deliveredOrder = buildDeliveredOrder(orderId, buyerId, deliveredAt);

        when(orderRepository.findWithDetailsById(orderId)).thenReturn(Optional.of(deliveredOrder));
        when(complaintRepository.existsByOrderId(orderId)).thenReturn(false);
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(inv -> {
            Complaint c = inv.getArgument(0);
            c.setId(401L);
            return c;
        });
        when(orderRepository.findDistinctFarmerIdsByOrderId(orderId)).thenReturn(List.of(farmerId));
        mockDetailDependencies(orderId);

        CreateComplaintRequest request = new CreateComplaintRequest(
            "QUALITY_NOT_AS_DESCRIBED",
            "Trái cây bị dập nát nhiều quả ở lớp dưới đáy thùng",
            List.of(
                new EvidenceItemRequest("IMAGE", "https://cdn.nongsanviet.vn/evidences/img-1.jpg", "Ảnh quả bị dập"),
                new EvidenceItemRequest("VIDEO", "https://cdn.nongsanviet.vn/evidences/vid-1.mp4", "Video mở thùng hàng")
            )
        );

        DisputeDetailResponse response = disputeService.createComplaint(buyerId, orderId, request);

        assertNotNull(response);
        assertEquals(401L, response.id());
        assertEquals(orderId, response.orderId());
        assertEquals("OPEN", response.status());
        assertEquals(2, response.evidences().size());
        assertEquals("IMAGE", response.evidences().get(0).mediaType());
        assertEquals("VIDEO", response.evidences().get(1).mediaType());

        // Gửi thông báo cho Farmer và ghi audit_logs
        verify(jdbcTemplate, times(1)).update(startsWith("INSERT INTO notifications"), eq(farmerId), anyString());
        verify(jdbcTemplate, times(1)).update(
            startsWith("INSERT INTO audit_logs"), eq(buyerId), eq("CREATE_COMPLAINT"), eq("complaints"), eq(401L)
        );
    }

    @Test
    @DisplayName("TC-COMPLAIN-01 (1b): Buyer tải trực tiếp tệp ảnh và video (multipart) trong vòng 24h -> lưu bền vững và tạo khiếu nại")
    void createComplaintWithMedia_Within24Hours_StoresFilesAndCreatesComplaint() {
        Long buyerId = 20L;
        Long orderId = 502L;
        OffsetDateTime deliveredAt = OffsetDateTime.of(2026, 6, 15, 8, 0, 0, 0, ZoneOffset.ofHours(7));
        // Mốc biên đúng 24h00m00s kể từ lúc giao hàng -> vẫn hợp lệ
        OffsetDateTime exactBoundary = deliveredAt.plusHours(24);
        disputeService.setClock(Clock.fixed(exactBoundary.toInstant(), ZoneOffset.ofHours(7)));

        Order deliveredOrder = buildDeliveredOrder(orderId, buyerId, deliveredAt);

        MockMultipartFile imageFile = new MockMultipartFile(
            "files", "qua-dap.jpg", "image/jpeg", "JPEG_BYTES".getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile videoFile = new MockMultipartFile(
            "files", "mo-hop.mp4", "video/mp4", "MP4_BYTES".getBytes(StandardCharsets.UTF_8)
        );

        when(orderRepository.findWithDetailsById(orderId)).thenReturn(Optional.of(deliveredOrder));
        when(complaintRepository.existsByOrderId(orderId)).thenReturn(false);
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(inv -> {
            Complaint c = inv.getArgument(0);
            if (c.getId() == null) {
                c.setId(402L);
            }
            return c;
        });
        when(complaintRepository.saveAndFlush(any(Complaint.class))).thenAnswer(inv -> {
            Complaint c = inv.getArgument(0);
            long idx = 1L;
            for (ComplaintEvidence ev : c.getEvidences()) {
                if (ev.getId() == null) {
                    ev.setId(800L + idx++);
                }
            }
            return c;
        });
        when(evidenceStorageService.store(eq(402L), eq(imageFile))).thenReturn(
            new StoredEvidenceFile("complaint-402/img.jpg", "qua-dap.jpg", "IMAGE", "image/jpeg", 10L, "sha256_img")
        );
        when(evidenceStorageService.store(eq(402L), eq(videoFile))).thenReturn(
            new StoredEvidenceFile("complaint-402/vid.mp4", "mo-hop.mp4", "VIDEO", "video/mp4", 9L, "sha256_vid")
        );
        when(orderRepository.findDistinctFarmerIdsByOrderId(orderId)).thenReturn(List.of(10L));

        ComplaintSubmissionResponse result = disputeService.createComplaintWithMedia(
            buyerId,
            orderId,
            "DAMAGED_GOODS",
            "Hàng bị hư hỏng khi nhận",
            List.of(imageFile, videoFile),
            List.of("Ảnh thực tế", "Video đồng kiểm")
        );

        assertEquals(402L, result.id());
        assertEquals("OPEN", result.status());
        assertEquals(deliveredAt, result.deliveredAt());
        assertEquals(deliveredAt.plusHours(24), result.slaDeadlineAt());
        assertEquals(2, result.evidences().size());
        assertEquals("/api/v1/disputes/evidences/801/content", result.evidences().get(0).mediaUrl());
        assertEquals("/api/v1/disputes/evidences/802/content", result.evidences().get(1).mediaUrl());
    }

    @Test
    @DisplayName("TC-COMPLAIN-01 (2a): Hệ thống từ chối khiếu nại gửi sau thời hạn 24h và thông báo rõ lý do")
    void createComplaint_After24HoursSla_ThrowsBadRequestWithClearReason() {
        Long buyerId = 20L;
        Long orderId = 503L;
        OffsetDateTime deliveredAt = OffsetDateTime.of(2026, 6, 15, 10, 0, 0, 0, ZoneOffset.ofHours(7));
        // Quá hạn 1 giây (24 giờ 00 phút 01 giây)
        OffsetDateTime expiredTime = deliveredAt.plusHours(24).plusSeconds(1);
        disputeService.setClock(Clock.fixed(expiredTime.toInstant(), ZoneOffset.ofHours(7)));

        Order deliveredOrder = buildDeliveredOrder(orderId, buyerId, deliveredAt);
        when(orderRepository.findWithDetailsById(orderId)).thenReturn(Optional.of(deliveredOrder));

        CreateComplaintRequest request = new CreateComplaintRequest(
            "SPOILED_PRODUCT",
            "Sản phẩm bị hỏng sau 1 ngày",
            List.of(new EvidenceItemRequest("IMAGE", "https://cdn.nongsanviet.vn/evidences/late.jpg", "Ảnh minh chứng"))
        );

        BusinessException ex = assertThrows(
            BusinessException.class,
            () -> disputeService.createComplaint(buyerId, orderId, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("COMPLAINT_SLA_EXPIRED", ex.getCode());
        assertTrue(ex.getMessage().contains("24 giờ"), "Thông báo lỗi phải nêu rõ thời hạn 24 giờ");
        assertTrue(ex.getMessage().contains("16/06/2026 10:00:00"), "Thông báo lỗi phải hiển thị mốc hạn chót cụ thể");

        verify(complaintRepository, never()).save(any());
        verify(evidenceStorageService, never()).store(anyLong(), any());
    }

    @Test
    @DisplayName("TC-COMPLAIN-01 (2b): Kiểm tra API checkComplaintEligibility trả về đúng trạng thái hết hạn SLA 24h")
    void checkComplaintEligibility_ExpiredOrder_ReturnsIneligibleWithReason() {
        Long buyerId = 20L;
        Long orderId = 504L;
        OffsetDateTime deliveredAt = OffsetDateTime.of(2026, 6, 15, 9, 0, 0, 0, ZoneOffset.ofHours(7));
        OffsetDateTime after30Hours = deliveredAt.plusHours(30);
        disputeService.setClock(Clock.fixed(after30Hours.toInstant(), ZoneOffset.ofHours(7)));

        Order deliveredOrder = buildDeliveredOrder(orderId, buyerId, deliveredAt);
        when(orderRepository.findWithDetailsById(orderId)).thenReturn(Optional.of(deliveredOrder));
        when(complaintRepository.existsByOrderId(orderId)).thenReturn(false);

        ComplaintEligibilityResponse eligibility = disputeService.checkComplaintEligibility(buyerId, orderId);

        assertFalse(eligibility.eligible());
        assertEquals("COMPLAINT_SLA_EXPIRED", eligibility.reasonCode());
        assertEquals(0L, eligibility.remainingSeconds());
        assertTrue(eligibility.message().contains("Đã quá thời hạn 24 giờ"));
    }

    @Test
    @DisplayName("TC-COMPLAIN-01 (2c): Từ chối khiếu nại nếu đơn hàng chưa giao (SHIPPING) hoặc thiếu ảnh/video minh chứng")
    void createComplaint_NotDeliveredOrMissingEvidence_ThrowsBadRequest() {
        Long buyerId = 20L;
        Long orderId = 505L;

        // 1. Đơn hàng đang giao (SHIPPING) chưa đánh dấu Đã giao
        Order shippingOrder = Order.builder()
            .id(orderId)
            .buyerId(buyerId)
            .status("SHIPPING")
            .build();
        when(orderRepository.findWithDetailsById(orderId)).thenReturn(Optional.of(shippingOrder));

        CreateComplaintRequest validPayload = new CreateComplaintRequest(
            "WRONG_ITEM",
            "Giao sai sản phẩm",
            List.of(new EvidenceItemRequest("IMAGE", "https://cdn.nongsanviet.vn/evidences/img.jpg", null))
        );

        BusinessException wrongStatusEx = assertThrows(
            BusinessException.class,
            () -> disputeService.createComplaint(buyerId, orderId, validPayload)
        );
        assertEquals(HttpStatus.BAD_REQUEST, wrongStatusEx.getStatus());
        assertEquals("INVALID_ORDER_STATUS_FOR_COMPLAINT", wrongStatusEx.getCode());

        // 2. Đơn hàng đã giao trong hạn 24h nhưng không đính kèm ảnh/video
        OffsetDateTime deliveredAt = OffsetDateTime.of(2026, 6, 15, 10, 0, 0, 0, ZoneOffset.ofHours(7));
        disputeService.setClock(Clock.fixed(deliveredAt.plusHours(1).toInstant(), ZoneOffset.ofHours(7)));
        Order deliveredOrder = buildDeliveredOrder(orderId, buyerId, deliveredAt);

        when(orderRepository.findWithDetailsById(orderId)).thenReturn(Optional.of(deliveredOrder));
        when(complaintRepository.existsByOrderId(orderId)).thenReturn(false);

        CreateComplaintRequest emptyEvidenceRequest = new CreateComplaintRequest(
            "DAMAGED_GOODS",
            "Hàng bị dập nhưng không gửi ảnh",
            List.of()
        );

        BusinessException noEvidenceEx = assertThrows(
            BusinessException.class,
            () -> disputeService.createComplaint(buyerId, orderId, emptyEvidenceRequest)
        );
        assertEquals(HttpStatus.BAD_REQUEST, noEvidenceEx.getStatus());
        assertEquals("EVIDENCE_REQUIRED", noEvidenceEx.getCode());
    }
}
