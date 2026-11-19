package com.nsv.backend.trace;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.inventory.entity.Batch;
import com.nsv.backend.inventory.repository.BatchRepository;
import com.nsv.backend.product.entity.Product;
import com.nsv.backend.product.repository.ProductRepository;
import com.nsv.backend.trace.dto.CreateFarmingLogRequest;
import com.nsv.backend.trace.dto.FarmingLogResponse;
import com.nsv.backend.trace.dto.TraceabilityResponse;
import com.nsv.backend.trace.entity.FarmingLog;
import com.nsv.backend.trace.repository.FarmingLogRepository;
import com.nsv.backend.trace.service.TraceabilityServiceImpl;
import com.nsv.backend.user.entity.Farm;
import com.nsv.backend.user.repository.FarmRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

/**
 * TC-DIARY-01 — PBI-27: Farmer cập nhật nhật ký trồng trọt theo mốc thời gian;
 * nhật ký hiển thị theo thứ tự thời gian và không thể xoá sau khi công khai (phần niêm phong
 * được kiểm thử ở TC-INTEGRITY-01).
 */
@ExtendWith(MockitoExtension.class)
class TcDiary01Test {

    private static final long FARMER_ID = 10L;
    private static final long BATCH_ID = 101L;

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

    private Batch ownedBatch() {
        return Batch.builder()
                .id(BATCH_ID).productId(1L).traceabilityCode(UUID.randomUUID())
                .harvestDate(LocalDate.of(2026, 6, 15)).unitPrice(new BigDecimal("45000.00"))
                .totalQuantity(new BigDecimal("200.00")).remainingQuantity(new BigDecimal("120.00"))
                .status("AVAILABLE").build();
    }

    private Product productOf(long farmerId) {
        return Product.builder().id(1L).farmerId(farmerId).categoryId(2L)
                .name("Vải thiều Thanh Hà").unit("kg").build();
    }

    @Test
    @DisplayName("TC-DIARY-01 (1a): Farmer thêm mục nhật ký với ngày + nội dung -> lưu thành công, nội dung được trim")
    void addFarmingLog_valid_savesLogWithDateAndTrimmedContent() {
        when(batchRepository.findById(BATCH_ID)).thenReturn(Optional.of(ownedBatch()));
        when(productRepository.findById(1L)).thenReturn(Optional.of(productOf(FARMER_ID)));
        when(farmingLogRepository.save(any(FarmingLog.class))).thenAnswer(inv -> {
            FarmingLog l = inv.getArgument(0);
            l.setId(1L);
            return l;
        });

        FarmingLogResponse response = traceabilityService.addFarmingLog(FARMER_ID, BATCH_ID,
                new CreateFarmingLogRequest(LocalDate.of(2026, 3, 10), "  Bón phân hữu cơ  ", "Phân chuồng", null));

        assertEquals(1L, response.id());
        assertEquals(LocalDate.of(2026, 3, 10), response.logDate());
        assertEquals("Bón phân hữu cơ", response.activity());
        assertEquals("Phân chuồng", response.note());

        ArgumentCaptor<FarmingLog> captor = ArgumentCaptor.forClass(FarmingLog.class);
        verify(farmingLogRepository).save(captor.capture());
        assertEquals(BATCH_ID, captor.getValue().getBatchId());
        assertEquals(LocalDate.of(2026, 3, 10), captor.getValue().getLogDate());
        assertEquals("Bón phân hữu cơ", captor.getValue().getActivity());
    }

    @Test
    @DisplayName("TC-DIARY-01 (1b): Farmer không sở hữu lô hàng không thể ghi nhật ký canh tác (403)")
    void addFarmingLog_notOwner_forbidden() {
        when(batchRepository.findById(BATCH_ID)).thenReturn(Optional.of(ownedBatch()));
        when(productRepository.findById(1L)).thenReturn(Optional.of(productOf(999L)));

        BusinessException ex = assertThrows(BusinessException.class, () -> traceabilityService.addFarmingLog(
                FARMER_ID, BATCH_ID,
                new CreateFarmingLogRequest(LocalDate.of(2026, 3, 10), "Bón phân", null, null)));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("BATCH_ACCESS_DENIED", ex.getCode());
        verify(farmingLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-DIARY-01 (1c): Lô hàng không tồn tại -> báo lỗi rõ ràng (404)")
    void addFarmingLog_batchNotFound() {
        when(batchRepository.findById(BATCH_ID)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> traceabilityService.addFarmingLog(
                FARMER_ID, BATCH_ID,
                new CreateFarmingLogRequest(LocalDate.of(2026, 3, 10), "Bón phân", null, null)));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("BATCH_NOT_FOUND", ex.getCode());
    }

    @SuppressWarnings("unchecked")
    private void mockMetadataLookups() {
        when(jdbcTemplate.query(startsWith("SELECT name FROM categories"),
                any(PreparedStatementSetter.class), any(ResultSetExtractor.class))).thenReturn("Trái cây");
        when(jdbcTemplate.query(startsWith("SELECT image_url FROM product_images"),
                any(PreparedStatementSetter.class), any(ResultSetExtractor.class))).thenReturn("img.jpg");
        when(jdbcTemplate.query(startsWith("SELECT full_name FROM users"),
                any(PreparedStatementSetter.class), any(ResultSetExtractor.class))).thenReturn("Nguyễn Văn An");
        when(jdbcTemplate.query(startsWith("SELECT name FROM provinces"),
                any(PreparedStatementSetter.class), any(ResultSetExtractor.class))).thenReturn("Hải Phòng");
    }

    @Test
    @DisplayName("TC-DIARY-01 (2): Nhật ký hiển thị theo đúng thứ tự thời gian (logDate tăng dần)")
    void farmingLogs_listedChronologicallyByDate() {
        Batch batch = ownedBatch();
        Product product = productOf(FARMER_ID);
        Farm farm = Farm.builder().id(5L).userId(FARMER_ID).provinceId(30L).mainCategoryId(2L)
                .farmName("Trang trại Thanh Sơn").addressLine("Thanh Hà")
                .certificateStatus("VERIFIED").build();

        List<FarmingLog> logs = List.of(
                FarmingLog.builder().id(1L).batchId(BATCH_ID).logDate(LocalDate.of(2026, 3, 10))
                        .activity("Bón phân hữu cơ").note("Phân chuồng").build(),
                FarmingLog.builder().id(2L).batchId(BATCH_ID).logDate(LocalDate.of(2026, 5, 2))
                        .activity("Tưới nước").note("Nhỏ giọt").build(),
                FarmingLog.builder().id(3L).batchId(BATCH_ID).logDate(LocalDate.of(2026, 6, 15))
                        .activity("Thu hoạch").note("Sáng sớm").build()
        );

        when(batchRepository.findById(BATCH_ID)).thenReturn(Optional.of(batch));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(farmRepository.findByUserId(FARMER_ID)).thenReturn(Optional.of(farm));
        when(farmingLogRepository.findAllByBatchIdOrderByLogDateAscIdAsc(BATCH_ID)).thenReturn(logs);
        mockMetadataLookups();

        TraceabilityResponse response = traceabilityService.getTraceabilityByBatchId(BATCH_ID);

        assertEquals(3, response.totalLogs());
        assertEquals(LocalDate.of(2026, 3, 10), response.farmingLogs().get(0).logDate());
        assertEquals(LocalDate.of(2026, 5, 2), response.farmingLogs().get(1).logDate());
        assertEquals(LocalDate.of(2026, 6, 15), response.farmingLogs().get(2).logDate());
        assertEquals("Bón phân hữu cơ", response.farmingLogs().get(0).activity());
        assertEquals("Thu hoạch", response.farmingLogs().get(2).activity());
    }
}
