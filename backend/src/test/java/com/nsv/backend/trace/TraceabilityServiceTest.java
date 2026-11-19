package com.nsv.backend.trace;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.inventory.entity.Batch;
import com.nsv.backend.inventory.repository.BatchRepository;
import com.nsv.backend.product.entity.Product;
import com.nsv.backend.product.repository.ProductRepository;
import com.nsv.backend.trace.dto.TraceabilityResponse;
import com.nsv.backend.trace.entity.FarmingLog;
import com.nsv.backend.trace.repository.FarmingLogRepository;
import com.nsv.backend.trace.service.TraceabilityServiceImpl;
import com.nsv.backend.user.entity.Farm;
import com.nsv.backend.user.repository.FarmRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraceabilityServiceTest {

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private FarmRepository farmRepository;

    @Mock
    private FarmingLogRepository farmingLogRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private TraceabilityServiceImpl traceabilityService;

    @SuppressWarnings("unchecked")
    private void mockMetadataLookups() {
        when(jdbcTemplate.query(
            startsWith("SELECT name FROM categories"),
            any(PreparedStatementSetter.class),
            any(ResultSetExtractor.class)
        )).thenReturn("Trái cây theo mùa");

        when(jdbcTemplate.query(
            startsWith("SELECT image_url FROM product_images"),
            any(PreparedStatementSetter.class),
            any(ResultSetExtractor.class)
        )).thenReturn("https://cdn.nongsanviet.vn/products/vai-thieu-1.jpg");

        when(jdbcTemplate.query(
            startsWith("SELECT full_name FROM users"),
            any(PreparedStatementSetter.class),
            any(ResultSetExtractor.class)
        )).thenReturn("Nguyễn Văn An");

        when(jdbcTemplate.query(
            startsWith("SELECT name FROM provinces"),
            any(PreparedStatementSetter.class),
            any(ResultSetExtractor.class)
        )).thenReturn("Hải Phòng");
    }

    @Test
    @DisplayName("TC-QR-01 (1): Mỗi lô hàng sở hữu mã QR duy nhất (UUID) gắn với thông tin truy xuất")
    void getTraceabilityByBatchId_ReturnsUniqueQrCodeAndUrl() {
        UUID codeBatch1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID codeBatch2 = UUID.fromString("22222222-2222-2222-2222-222222222222");

        Batch batch1 = Batch.builder()
            .id(101L).productId(1L).traceabilityCode(codeBatch1)
            .harvestDate(LocalDate.of(2026, 6, 10)).unitPrice(new BigDecimal("45000.00"))
            .totalQuantity(new BigDecimal("200.00")).remainingQuantity(new BigDecimal("150.00"))
            .status("AVAILABLE").build();

        Batch batch2 = Batch.builder()
            .id(102L).productId(1L).traceabilityCode(codeBatch2)
            .harvestDate(LocalDate.of(2026, 6, 20)).unitPrice(new BigDecimal("48000.00"))
            .totalQuantity(new BigDecimal("300.00")).remainingQuantity(new BigDecimal("300.00"))
            .status("PRE_ORDER").build();

        Product product = Product.builder()
            .id(1L).farmerId(10L).categoryId(2L).name("Vải thiều Thanh Hà").unit("kg").build();
        Farm farm = Farm.builder()
            .id(5L).userId(10L).provinceId(30L).mainCategoryId(2L)
            .farmName("Trang trại Vải thiều Thanh Hà").addressLine("Xã Thanh Sơn, Thanh Hà")
            .certificateStatus("VERIFIED").build();

        when(batchRepository.findById(101L)).thenReturn(Optional.of(batch1));
        when(batchRepository.findById(102L)).thenReturn(Optional.of(batch2));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(farmRepository.findByUserId(10L)).thenReturn(Optional.of(farm));
        when(farmingLogRepository.findAllByBatchIdOrderByLogDateAscIdAsc(anyLong())).thenReturn(List.of());
        mockMetadataLookups();

        TraceabilityResponse trace1 = traceabilityService.getTraceabilityByBatchId(101L);
        TraceabilityResponse trace2 = traceabilityService.getTraceabilityByBatchId(102L);

        assertEquals(codeBatch1, trace1.batch().traceabilityCode());
        assertEquals(codeBatch2, trace2.batch().traceabilityCode());
        assertNotEquals(trace1.batch().traceabilityCode(), trace2.batch().traceabilityCode());
        assertTrue(trace1.batch().qrScanUrl().endsWith(codeBatch1.toString()));
        assertTrue(trace2.batch().qrScanUrl().endsWith(codeBatch2.toString()));
    }

    @Test
    @DisplayName("TC-QR-01 (2a): Quét mã QR hiển thị đầy đủ thông tin trang trại, sản phẩm và nhật ký trồng trọt")
    void scanQrCode_ValidCode_ReturnsCompleteFarmAndFarmingLogs() {
        UUID qrCode = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        Batch batch = Batch.builder()
            .id(101L).productId(1L).traceabilityCode(qrCode)
            .harvestDate(LocalDate.of(2026, 6, 15))
            .unitPrice(new BigDecimal("45000.00"))
            .totalQuantity(new BigDecimal("200.00"))
            .remainingQuantity(new BigDecimal("120.00"))
            .status("AVAILABLE")
            .build();

        Product product = Product.builder()
            .id(1L).farmerId(10L).categoryId(2L)
            .name("Vải thiều Thanh Hà VietGAP")
            .description("Vải thiều chín sớm, ngọt thanh, cùi dày")
            .unit("kg")
            .build();

        Farm farm = Farm.builder()
            .id(5L).userId(10L).provinceId(30L).mainCategoryId(2L)
            .farmName("Trang trại Sinh thái Thanh Sơn")
            .addressLine("Thôn 2, Xã Thanh Sơn, Huyện Thanh Hà")
            .certificateStatus("VERIFIED")
            .certificateUrl("https://cdn.nongsanviet.vn/certs/vietgap-05.pdf")
            .build();

        List<FarmingLog> logs = List.of(
            FarmingLog.builder()
                .id(1L).batchId(101L).logDate(LocalDate.of(2026, 3, 10))
                .activity("Bón phân hữu cơ vi sinh")
                .note("Bón lót phân chuồng hoai mục 5kg/gốc")
                .imageUrl("https://cdn.nongsanviet.vn/logs/log-1.jpg")
                .build(),
            FarmingLog.builder()
                .id(2L).batchId(101L).logDate(LocalDate.of(2026, 5, 2))
                .activity("Tưới nước & tỉa cành tạo tán")
                .note("Tưới nhỏ giọt tự động, không sử dụng thuốc trừ sâu hóa học")
                .build(),
            FarmingLog.builder()
                .id(3L).batchId(101L).logDate(LocalDate.of(2026, 6, 15))
                .activity("Thu hoạch & đóng gói đạt chuẩn")
                .note("Thu hoạch sáng sớm, phân loại quả đồng đều")
                .build()
        );

        when(batchRepository.findByTraceabilityCode(qrCode)).thenReturn(Optional.of(batch));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(farmRepository.findByUserId(10L)).thenReturn(Optional.of(farm));
        when(farmingLogRepository.findAllByBatchIdOrderByLogDateAscIdAsc(101L)).thenReturn(logs);
        mockMetadataLookups();

        TraceabilityResponse response = traceabilityService.getTraceabilityByCode(qrCode.toString());

        assertNotNull(response);
        assertEquals(qrCode, response.batch().traceabilityCode());
        assertEquals("Vải thiều Thanh Hà VietGAP", response.product().productName());
        assertEquals("Trang trại Sinh thái Thanh Sơn", response.farm().farmName());
        assertEquals("Hải Phòng", response.farm().provinceName());
        assertEquals("VERIFIED", response.farm().certificateStatus());
        assertEquals(3, response.totalLogs());
        assertEquals("Bón phân hữu cơ vi sinh", response.farmingLogs().get(0).activity());
        assertEquals("Thu hoạch & đóng gói đạt chuẩn", response.farmingLogs().get(2).activity());
        assertNotNull(response.verifiedAt());
    }

    @Test
    @DisplayName("TC-QR-01 (2b): Quét mã QR sai định dạng hoặc không tồn tại trả về lỗi rõ ràng")
    void scanQrCode_InvalidOrUnknownCode_ThrowsBusinessException() {
        BusinessException invalidFormat = assertThrows(
            BusinessException.class,
            () -> traceabilityService.getTraceabilityByCode("ma-qr-khong-hop-le")
        );
        assertEquals(HttpStatus.BAD_REQUEST, invalidFormat.getStatus());
        assertEquals("INVALID_TRACEABILITY_CODE", invalidFormat.getCode());

        UUID unknownCode = UUID.fromString("99999999-9999-9999-9999-999999999999");
        when(batchRepository.findByTraceabilityCode(unknownCode)).thenReturn(Optional.empty());

        BusinessException notFound = assertThrows(
            BusinessException.class,
            () -> traceabilityService.getTraceabilityByCode(unknownCode.toString())
        );
        assertEquals(HttpStatus.NOT_FOUND, notFound.getStatus());
        assertEquals("TRACEABILITY_CODE_NOT_FOUND", notFound.getCode());
    }
}
