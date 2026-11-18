package com.nsv.backend.product;

import com.nsv.backend.common.exception.ApiException;
import com.nsv.backend.inventory.entity.Batch;
import com.nsv.backend.inventory.repository.BatchRepository;
import com.nsv.backend.product.dto.BatchCreateRequest;
import com.nsv.backend.product.dto.BatchResponse;
import com.nsv.backend.product.dto.BatchUpdateRequest;
import com.nsv.backend.product.entity.Product;
import com.nsv.backend.product.repository.ProductRepository;
import com.nsv.backend.product.service.BatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Kiểm thử PBI-04 ở mức service. "Hôm nay" cố định là 2026-11-17. */
@ExtendWith(MockitoExtension.class)
class BatchServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 17);
    private static final Long FARMER_ID = 10L;

    @Mock
    private BatchRepository batchRepository;
    @Mock
    private ProductRepository productRepository;

    private BatchService service;
    private final Product product = Product.builder()
            .id(1L).farmerId(FARMER_ID).name("Cà chua bi").unit("kg").build();

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-17T03:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
        service = new BatchService(batchRepository, productRepository, clock);
    }

    private BatchCreateRequest createReq(LocalDate date) {
        return new BatchCreateRequest(1L, new BigDecimal("120.50"), new BigDecimal("15000"), date);
    }

    private BatchUpdateRequest updateReq(LocalDate date) {
        return new BatchUpdateRequest(new BigDecimal("200"), new BigDecimal("18000"), date);
    }

    private Batch batch(long id, String status) {
        return Batch.builder().id(id).product(product).traceabilityCode(UUID.randomUUID())
                .harvestDate(TODAY.plusDays(7)).unitPrice(new BigDecimal("15000"))
                .totalQuantity(new BigDecimal("120.50")).status(status).build();
    }

    // ---- Tạo ----

    @Test
    void create_valid_savesPreOrderBatchWithTraceabilityCode() {
        when(productRepository.findByIdAndFarmerId(1L, FARMER_ID)).thenReturn(Optional.of(product));
        when(batchRepository.save(any(Batch.class))).thenAnswer(inv -> {
            Batch b = inv.getArgument(0);
            b.setId(1L);
            return b;
        });

        BatchResponse res = service.create(FARMER_ID, createReq(TODAY.plusDays(7)));

        assertThat(res.id()).isEqualTo(1L);
        assertThat(res.productName()).isEqualTo("Cà chua bi");
        assertThat(res.unit()).isEqualTo("kg");
        ArgumentCaptor<Batch> captor = ArgumentCaptor.forClass(Batch.class);
        verify(batchRepository).save(captor.capture());
        Batch saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo("PRE_ORDER");
        assertThat(saved.getTraceabilityCode()).isNotNull();
        assertThat(saved.getHarvestDate()).isEqualTo(TODAY.plusDays(7));
        assertThat(saved.getTotalQuantity()).isEqualByComparingTo("120.50");
        assertThat(saved.getUnitPrice()).isEqualByComparingTo("15000");
    }

    @Test
    void create_harvestDateToday_isAccepted() {
        when(productRepository.findByIdAndFarmerId(1L, FARMER_ID)).thenReturn(Optional.of(product));
        when(batchRepository.save(any(Batch.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.create(FARMER_ID, createReq(TODAY)).harvestDate()).isEqualTo(TODAY);
    }

    @Test
    void create_harvestDateInPast_isRejectedWithFieldError() {
        assertThatThrownBy(() -> service.create(FARMER_ID, createReq(TODAY.minusDays(1))))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ex.getFieldErrors()).extracting("field").containsExactly("harvestDate");
                });
        verify(batchRepository, never()).save(any());
    }

    @Test
    void create_harvestDateBeyondOneYear_isRejected() {
        assertThatThrownBy(() -> service.create(FARMER_ID, createReq(TODAY.plusDays(366))))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void create_harvestDateExactlyOneYearAhead_isAccepted() {
        when(productRepository.findByIdAndFarmerId(1L, FARMER_ID)).thenReturn(Optional.of(product));
        when(batchRepository.save(any(Batch.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.create(FARMER_ID, createReq(TODAY.plusDays(365))).harvestDate())
                .isEqualTo(TODAY.plusDays(365));
    }

    @Test
    void create_productNotOwnedByFarmer_returnsNotFound() {
        when(productRepository.findByIdAndFarmerId(1L, FARMER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(FARMER_ID, createReq(TODAY.plusDays(7))))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
        verify(batchRepository, never()).save(any());
    }

    // ---- Xem ----

    @Test
    void list_all_usesFarmerOnly() {
        when(batchRepository.findAllByFarmer(FARMER_ID))
                .thenReturn(List.of(batch(1, "PRE_ORDER"), batch(2, "AVAILABLE")));

        assertThat(service.list(FARMER_ID, false)).hasSize(2);
    }

    @Test
    void list_upcomingOnly_filtersFromToday() {
        when(batchRepository.findUpcomingByFarmer(FARMER_ID, TODAY))
                .thenReturn(List.of(batch(3, "PRE_ORDER")));

        assertThat(service.list(FARMER_ID, true)).extracting("id").containsExactly(3L);
    }

    @Test
    void get_batchOfAnotherFarmer_returnsNotFound() {
        when(batchRepository.findByIdAndFarmer(99L, FARMER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(FARMER_ID, 99L))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    // ---- Sửa ----

    @Test
    void update_preOrderBatch_changesFields() {
        when(batchRepository.findByIdAndFarmer(5L, FARMER_ID)).thenReturn(Optional.of(batch(5, "PRE_ORDER")));
        when(batchRepository.save(any(Batch.class))).thenAnswer(inv -> inv.getArgument(0));

        BatchResponse res = service.update(FARMER_ID, 5L, updateReq(TODAY.plusDays(30)));

        assertThat(res.totalQuantity()).isEqualByComparingTo("200");
        assertThat(res.unitPrice()).isEqualByComparingTo("18000");
        assertThat(res.harvestDate()).isEqualTo(TODAY.plusDays(30));
    }

    @Test
    void update_pastDate_isRejectedAndNothingSaved() {
        when(batchRepository.findByIdAndFarmer(5L, FARMER_ID)).thenReturn(Optional.of(batch(5, "PRE_ORDER")));

        assertThatThrownBy(() -> service.update(FARMER_ID, 5L, updateReq(TODAY.minusDays(3))))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(batchRepository, never()).save(any());
    }

    @Test
    void update_batchNotInPreOrder_isRejectedWithConflict() {
        when(batchRepository.findByIdAndFarmer(5L, FARMER_ID)).thenReturn(Optional.of(batch(5, "AVAILABLE")));

        assertThatThrownBy(() -> service.update(FARMER_ID, 5L, updateReq(TODAY.plusDays(10))))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(ex.getCode()).isEqualTo("INVALID_STATE");
                });
        verify(batchRepository, never()).save(any());
    }

    @Test
    void update_batchOfAnotherFarmer_returnsNotFound() {
        when(batchRepository.findByIdAndFarmer(7L, FARMER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(FARMER_ID, 7L, updateReq(TODAY.plusDays(2))))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
