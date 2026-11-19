package com.nsv.backend.product;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.product.dto.CreateProductRequest;
import com.nsv.backend.product.dto.ProductSummaryResponse;
import com.nsv.backend.product.entity.Product;
import com.nsv.backend.product.entity.ProductImage;
import com.nsv.backend.product.repository.ProductImageRepository;
import com.nsv.backend.product.repository.ProductRepository;
import com.nsv.backend.product.service.ProductServiceImpl;
import com.nsv.backend.product.spi.ProductBatchPort;
import com.nsv.backend.product.spi.ProductBatchSnapshot;
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
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TcProd01Test {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private ProductBatchPort productBatchPort;

    @Mock
    private FarmRepository farmRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ProductServiceImpl productService;

    @SuppressWarnings("unchecked")
    private void mockSingleStringQuery(String prefix, String value) {
        when(jdbcTemplate.query(
            startsWith(prefix),
            any(PreparedStatementSetter.class),
            any(ResultSetExtractor.class)
        )).thenReturn(value);
    }

    @Test
    @DisplayName("TC-PROD-01 (1a): Farmer hoàn tất đăng bán sản phẩm (tên, giá, ảnh, số lượng) trong 1 lần gửi (<= 3 bước)")
    void createProductInQuickFlow_CompletePayload_CreatesProductImagesAndBatchAtomically() {
        Long farmerId = 10L;
        Farm farm = Farm.builder()
            .id(5L)
            .userId(farmerId)
            .provinceId(30L)
            .mainCategoryId(2L)
            .farmName("Trang trại Sinh thái Thanh Sơn")
            .certificateStatus("VERIFIED")
            .build();

        CreateProductRequest request = new CreateProductRequest(
            "Vải thiều Thanh Hà chính vụ",
            2L,
            "kg",
            "Vải thiều cùi dày, ngọt thanh, thu hoạch sáng sớm tại vườn",
            List.of(
                "https://cdn.nongsanviet.vn/products/vai-thieu-main.jpg",
                "https://cdn.nongsanviet.vn/products/vai-thieu-detail.jpg"
            ),
            new BigDecimal("42000.00"),
            new BigDecimal("250.00"),
            LocalDate.of(2026, 6, 12),
            "AVAILABLE"
        );

        when(farmRepository.findByUserId(farmerId)).thenReturn(Optional.of(farm));
        mockSingleStringQuery("SELECT name FROM categories", "Trái cây theo mùa");
        mockSingleStringQuery("SELECT name FROM provinces", "Hải Phòng");

        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(1001L);
            p.setCreatedAt(OffsetDateTime.now());
            return p;
        });
        when(productImageRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        UUID batchCode = UUID.randomUUID();
        when(productBatchPort.createInitialBatch(
            eq(1001L),
            eq(new BigDecimal("42000.00")),
            eq(new BigDecimal("250.00")),
            eq(LocalDate.of(2026, 6, 12)),
            eq("AVAILABLE")
        )).thenReturn(new ProductBatchSnapshot(
            5001L,
            1001L,
            batchCode,
            LocalDate.of(2026, 6, 12),
            new BigDecimal("42000.00"),
            new BigDecimal("250.00"),
            new BigDecimal("250.00"),
            "AVAILABLE"
        ));

        ProductSummaryResponse response = productService.createProductInQuickFlow(farmerId, request);

        assertNotNull(response);
        assertEquals(1001L, response.id());
        assertEquals("Vải thiều Thanh Hà chính vụ", response.name());
        assertEquals(new BigDecimal("42000.00"), response.unitPrice());
        assertEquals(new BigDecimal("250.00"), response.remainingQuantity());
        assertEquals("https://cdn.nongsanviet.vn/products/vai-thieu-main.jpg", response.primaryImageUrl());
        assertEquals(2, response.imageUrls().size());
        assertEquals(5001L, response.batchId());
        assertNotNull(response.traceabilityCode());
        assertEquals("AVAILABLE", response.batchStatus());
        assertEquals("Trang trại Sinh thái Thanh Sơn", response.farmName());
        assertTrue(response.farmVerified());

        verify(productRepository, times(1)).save(any(Product.class));
        verify(productImageRepository, times(1)).saveAll(anyList());
        verify(productBatchPort, times(1)).createInitialBatch(
            eq(1001L),
            eq(new BigDecimal("42000.00")),
            eq(new BigDecimal("250.00")),
            eq(LocalDate.of(2026, 6, 12)),
            eq("AVAILABLE")
        );
        verify(jdbcTemplate, times(1)).update(
            startsWith("INSERT INTO audit_logs"),
            eq(farmerId),
            eq("CREATE_PRODUCT"),
            eq("products"),
            eq(1001L)
        );
    }

    @Test
    @DisplayName("TC-PROD-01 (1b): Chặn đăng bán nếu Farmer chưa có hồ sơ trang trại")
    void createProductInQuickFlow_NoFarmProfile_ThrowsBadRequest() {
        Long farmerId = 99L;
        CreateProductRequest request = new CreateProductRequest(
            "Ổi bo Hải Phòng",
            2L,
            "kg",
            null,
            List.of("https://cdn.nongsanviet.vn/products/oi-bo.jpg"),
            new BigDecimal("25000.00"),
            new BigDecimal("100.00"),
            null,
            null
        );

        when(farmRepository.findByUserId(farmerId)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(
            BusinessException.class,
            () -> productService.createProductInQuickFlow(farmerId, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("FARM_PROFILE_REQUIRED", ex.getCode());
        verify(productRepository, never()).save(any());
        verifyNoInteractions(productBatchPort);
    }

    @Test
    @DisplayName("TC-PROD-01 (2): Sản phẩm sau khi đăng hiển thị ngay trên trang chủ và trang tìm kiếm đúng đầy đủ thông tin")
    void getPublicProducts_HomepageAndKeywordSearch_ReturnsNewlyListedProductImmediately() {
        Product newlyCreated = Product.builder()
            .id(1001L)
            .farmerId(10L)
            .categoryId(2L)
            .name("Vải thiều Thanh Hà chính vụ")
            .description("Ngọt thanh, chuẩn VietGAP")
            .unit("kg")
            .createdAt(OffsetDateTime.now())
            .build();

        ProductImage primaryImg = ProductImage.builder()
            .id(1L)
            .productId(1001L)
            .imageUrl("https://cdn.nongsanviet.vn/products/vai-thieu-main.jpg")
            .sortOrder(0)
            .build();

        UUID qrCode = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        ProductBatchSnapshot activeBatch = new ProductBatchSnapshot(
            5001L,
            1001L,
            qrCode,
            LocalDate.of(2026, 6, 12),
            new BigDecimal("42000.00"),
            new BigDecimal("250.00"),
            new BigDecimal("250.00"),
            "AVAILABLE"
        );

        Farm farm = Farm.builder()
            .id(5L)
            .userId(10L)
            .provinceId(30L)
            .mainCategoryId(2L)
            .farmName("Trang trại Sinh thái Thanh Sơn")
            .certificateStatus("VERIFIED")
            .build();

        when(productRepository.searchPublicProducts("Vải thiều", null, null, null, null))
            .thenReturn(List.of(newlyCreated));
        when(productImageRepository.findAllByProductIdInOrderBySortOrderAscIdAsc(Set.of(1001L)))
            .thenReturn(List.of(primaryImg));
        when(productBatchPort.findLatestActiveBatches(
            Set.of(1001L), List.of("AVAILABLE", "PRE_ORDER")
        )).thenReturn(Map.of(1001L, activeBatch));
        when(farmRepository.findAll()).thenReturn(List.of(farm));

        mockSingleStringQuery("SELECT name FROM categories", "Trái cây theo mùa");
        mockSingleStringQuery("SELECT name FROM provinces", "Hải Phòng");

        List<ProductSummaryResponse> searchResults = productService.getPublicProducts(
            "  Vải thiều  ", null, null, null, null
        );

        assertEquals(1, searchResults.size());
        ProductSummaryResponse item = searchResults.get(0);
        assertEquals(1001L, item.id());
        assertEquals("Vải thiều Thanh Hà chính vụ", item.name());
        assertEquals(new BigDecimal("42000.00"), item.unitPrice());
        assertEquals(new BigDecimal("250.00"), item.remainingQuantity());
        assertEquals("https://cdn.nongsanviet.vn/products/vai-thieu-main.jpg", item.primaryImageUrl());
        assertEquals(qrCode, item.traceabilityCode());
        assertEquals("Trang trại Sinh thái Thanh Sơn", item.farmName());
        assertEquals("Hải Phòng", item.provinceName());
        assertTrue(item.farmVerified());
    }
}
